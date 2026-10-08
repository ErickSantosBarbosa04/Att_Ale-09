package br.com.jurishome.auth.config;

import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ui")
public class UiProperties {

    // O tema vem da configuração do servidor e só pode selecionar um arquivo conhecido.
    @Pattern(regexp = "juris|neutro", message = "Tema deve ser juris ou neutro")
    private String theme = "juris";

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
