package br.com.fatecads.fatecads.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.fatecads.fatecads.entity.Usuario;
import br.com.fatecads.fatecads.repository.UsuarioRepository;
import br.com.fatecads.fatecads.security.Roles;

@Configuration
public class InitialDataSeeder {

    private static final String ADMIN_EMAIL = "super@super";
    private static final String ADMIN_LOGIN = "super";
    private static final String ADMIN_PASSWORD = "password";

    @Bean
    CommandLineRunner seedDefaultAdmin(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByLoginUsuario(ADMIN_LOGIN).isPresent()) {
                return;
            }

            Usuario admin = new Usuario();
            admin.setNomeUsuario("Super Administrador");
            admin.setEmailUsuario(ADMIN_EMAIL);
            admin.setLoginUsuario(ADMIN_LOGIN);
            admin.setSenhaUsuario(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setRole(Roles.ADMIN);
            usuarioRepository.save(admin);
        };
    }
}
