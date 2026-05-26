package br.com.fatecads.fatecads.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.fatecads.fatecads.service.auth.PasswordRecoveryService;

@Controller
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @GetMapping("/esqueci-senha")
    public String telaEsqueciSenha() {
        return "auth/esqueciSenha";
    }

    @PostMapping("/esqueci-senha")
    public String enviarLink(
            @RequestParam String email,
            @RequestParam String telefone,
            RedirectAttributes redirectAttributes) {

        boolean enviado = passwordRecoveryService.solicitarRedefinicao(email, telefone);
        if (enviado) {
            redirectAttributes.addFlashAttribute("sucesso", "Mensagem enviada por WhatsApp com o link de redefinicao.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Email nao encontrado ou falha no envio do WhatsApp.");
        }

        return "redirect:/esqueci-senha";
    }

    @GetMapping("/redefinir-senha/{token}")
    public String telaRedefinir(@PathVariable String token, Model model) {
        if (!passwordRecoveryService.tokenValido(token)) {
            model.addAttribute("erro", "Link invalido ou expirado.");
            return "auth/redefinirSenha";
        }

        model.addAttribute("token", token);
        return "auth/redefinirSenha";
    }

    @GetMapping("/redefinir-senha")
    public String telaRedefinirSemToken(Model model) {
        model.addAttribute("erro", "Link invalido ou expirado.");
        return "auth/redefinirSenha";
    }

    @PostMapping("/redefinir-senha/{token}")
    public String redefinirSenha(
            @PathVariable String token,
            @RequestParam String novaSenha,
            RedirectAttributes redirectAttributes) {

        boolean atualizado = passwordRecoveryService.redefinirSenha(token, novaSenha);
        if (!atualizado) {
            redirectAttributes.addFlashAttribute("erro", "Nao foi possivel redefinir. Gere um novo link.");
            return "redirect:/esqueci-senha";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Senha redefinida com sucesso. Faca login novamente.");
        return "redirect:/login";
    }
}
