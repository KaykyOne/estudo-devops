package br.com.fatecads.fatecads.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fatecads.fatecads.entity.Usuario;
import br.com.fatecads.fatecads.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario save(Usuario usuario) {
        usuario.setRole(normalizarRole(usuario.getRole()));
        if (!senhaJaCriptografada(usuario.getSenha())) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findByTokenRedefinicaoSenha(String token) {
        return usuarioRepository.findByTokenRedefinicaoSenha(token);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario update(Usuario usuario) {
        if (usuario.getIdUsuario() != null && usuarioRepository.existsById(usuario.getIdUsuario())) {
            usuario.setRole(normalizarRole(usuario.getRole()));
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    public Usuario updatePassword(Usuario usuario, String novaSenha) {
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        return usuarioRepository.save(usuario);
    }

    private boolean senhaJaCriptografada(String senha) {
        return senha != null && (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$"));
    }

    private String normalizarRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String roleNormalizada = role.trim().toUpperCase();
        if (!roleNormalizada.startsWith("ROLE_")) {
            roleNormalizada = "ROLE_" + roleNormalizada;
        }
        return roleNormalizada;
    }
}
