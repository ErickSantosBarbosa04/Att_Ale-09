package br.com.jurishome.auth.bootstrap;

import br.com.jurishome.auth.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InitialAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminSeeder.class);

    private final UserService users;
    private final String username;
    private final String email;
    private final String password;

    public InitialAdminSeeder(UserService users,
        @Value("${app.bootstrap.admin-username:}") String username,
        @Value("${app.bootstrap.admin-email:}") String email,
        @Value("${app.bootstrap.admin-password:}") String password) {
        this.users = users;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.info("Administrador inicial não configurado; defina as três variáveis INITIAL_ADMIN_* para criá-lo.");
            return;
        }
        if (password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            log.warn("Administrador inicial não criado: a senha deve ter entre 12 e 72 bytes em UTF-8.");
            return;
        }
        boolean created = users.createInitialAdmin(username, email, password);
        log.info(created
            ? "Administrador inicial criado. A senha não é exibida nos logs."
            : "Administrador inicial não criado porque usuário ou e-mail já existe.");
    }
}
