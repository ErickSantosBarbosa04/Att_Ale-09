package br.com.jurishome.auth.web;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/painel")
    public String routeToRoleArea(Authentication authentication) {
        Collection<String> authorities = authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority()).toList();
        if (authorities.contains("ROLE_ADMIN")) {
            return "redirect:/admin/usuarios";
        }
        if (authorities.contains("ROLE_ADVOGADO")) {
            return "redirect:/advogado/area";
        }
        return "redirect:/cliente/area";
    }

    @GetMapping("/cliente/area")
    public String clientArea() {
        return "areas/cliente";
    }

    @GetMapping("/advogado/area")
    public String lawyerArea() {
        return "areas/advogado";
    }
}
