package br.com.jurishome.auth.web;

import br.com.jurishome.auth.config.UiProperties;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    private final UiProperties uiProperties;

    public GlobalModelAdvice(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @ModelAttribute
    public void addSharedViewData(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("ui", uiProperties);
        if (principal != null) {
            List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
            model.addAttribute("auth", new AuthView(principal.getUsername(), roles));
        }
    }
}
