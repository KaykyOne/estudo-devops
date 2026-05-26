package br.com.fatecads.fatecads.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idUsuario;

    @Column(nullable = false, length = 40)
    private String nome;

    @Column(nullable = false, length = 40)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, length = 20)
    private String login;

    @Column(nullable = false, length = 150)
    private String senha;

    @Column(length = 1000)
    private String tokenRedefinicaoSenha;

    private LocalDateTime tokenRedefinicaoExpiraEm;
    
    private String role = "ROLE_USER";
}
