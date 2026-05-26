package br.com.fatecads.fatecads.service.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.fatecads.fatecads.entity.Usuario;
import br.com.fatecads.fatecads.repository.UsuarioRepository;
import br.com.fatecads.fatecads.service.UsuarioService;

@Service
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final HttpClient httpClient;

    @Value("${app.whatsapp.url}")
    private String whatsappUrl;

    @Value("${app.reset.base-url}")
    private String baseUrl;

    @Value("${app.reset.expiration-minutes}")
    private long resetExpirationMinutes;

    public PasswordRecoveryService(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean solicitarRedefinicao(String email, String telefone) {
        Optional<Usuario> usuarioOptional = usuarioService.findByEmail(email);
        if (usuarioOptional.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOptional.get();
        String token = UUID.randomUUID().toString();

        usuario.setTokenRedefinicaoSenha(token);
        usuario.setTokenRedefinicaoExpiraEm(LocalDateTime.now().plusMinutes(resetExpirationMinutes));
        usuarioRepository.save(usuario);

        String link = montarLinkRedefinicao(token);
        String texto = link;

        try {
            enviarMensagemWhatsapp(texto, telefone);
            return true;
        } catch (RuntimeException ex) {
            usuario.setTokenRedefinicaoSenha(null);
            usuario.setTokenRedefinicaoExpiraEm(null);
            usuarioRepository.save(usuario);
            return false;
        }
    }

    public boolean tokenValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Optional<Usuario> usuarioOptional = usuarioService.findByTokenRedefinicaoSenha(token);
        if (usuarioOptional.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOptional.get();
        LocalDateTime validade = usuario.getTokenRedefinicaoExpiraEm();
        if (validade == null || validade.isBefore(LocalDateTime.now())) {
            return false;
        }

        return token.equals(usuario.getTokenRedefinicaoSenha());
    }

    public boolean redefinirSenha(String token, String novaSenha) {
        Optional<Usuario> usuarioOptional = usuarioService.findByTokenRedefinicaoSenha(token);
        if (usuarioOptional.isEmpty() || !tokenValido(token)) {
            return false;
        }

        Usuario usuario = usuarioOptional.get();
        usuarioService.updatePassword(usuario, novaSenha);

        usuario.setTokenRedefinicaoSenha(null);
        usuario.setTokenRedefinicaoExpiraEm(null);
        usuarioRepository.save(usuario);

        return true;
    }

    private String montarLinkRedefinicao(String token) {
        String tokenUrl = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return baseUrl + "/redefinir-senha/" + tokenUrl;
    }

    private void enviarMensagemWhatsapp(String texto, String telefone) {
        String body = "{\"text\":\"" + jsonEscape(texto) + "\",\"phone\":\"" + jsonEscape(telefone) + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(whatsappUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Falha ao enviar WhatsApp. HTTP " + response.statusCode());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Falha ao enviar WhatsApp.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao enviar WhatsApp.", ex);
        }
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
