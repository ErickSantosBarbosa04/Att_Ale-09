package br.com.jurishome.auth.web;

import br.com.jurishome.auth.user.RegistrationForm;
import br.com.jurishome.auth.user.UserService;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String registrationForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult result) {
        if (!java.util.Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "As senhas não coincidem.");
        }
        if (result.hasErrors()) {
            return "auth/cadastro";
        }

        try {
            users.registerClient(form);
        } catch (DuplicateKeyException duplicate) {
            // Índices únicos no MongoDB fecham a janela entre uma verificação e a gravação.
            result.reject("duplicate", "Não foi possível concluir o cadastro. Confira usuário e e-mail.");
            return "auth/cadastro";
        }
        return "redirect:/login?cadastro";
    }
}
