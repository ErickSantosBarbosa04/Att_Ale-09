package br.com.jurishome.auth.user;

import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Informe seu nome.")
    @Size(min = 2, max = 80, message = "Use entre 2 e 80 caracteres.")
    private String displayName;

    @NotBlank(message = "Informe um usuário.")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,32}$",
        message = "Use 3 a 32 letras, números, ponto, hífen ou sublinhado.")
    private String username;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
    private String email;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 12, max = 72, message = "Use uma senha entre 12 e 72 caracteres.")
    private String password;

    @NotBlank(message = "Confirme sua senha.")
    private String confirmPassword;

    @AssertTrue(message = "A senha não pode ultrapassar 72 bytes em UTF-8.")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
