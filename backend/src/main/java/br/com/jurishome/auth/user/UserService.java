package br.com.jurishome.auth.user;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder,
                       FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.sessions = sessions;
    }

    public void registerClient(RegistrationForm form) {
        Instant now = Instant.now();
        AppUser user = new AppUser(
            form.getDisplayName().trim(),
            normalize(form.getUsername()),
            normalize(form.getEmail()),
            passwordEncoder.encode(form.getPassword()),
            EnumSet.of(Role.CLIENTE),
            true,
            now,
            now
        );
        // O formulário público nunca recebe nem define um perfil privilegiado.
        users.save(user);
    }

    public boolean createInitialAdmin(String username, String email, String rawPassword) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);
        if (users.existsByUsername(normalizedUsername) || users.existsByEmail(normalizedEmail)) {
            return false;
        }

        Instant now = Instant.now();
        try {
            users.save(new AppUser(
                "Administrador inicial", normalizedUsername, normalizedEmail,
                passwordEncoder.encode(rawPassword), EnumSet.of(Role.ADMIN), true, now, now
            ));
            return true;
        } catch (DuplicateKeyException concurrentBootstrap) {
            // Duas instâncias podem iniciar juntas; o índice único escolhe uma delas.
            return false;
        }
    }

    public void changeRole(String userId, Role role, String actingUsername) {
        AppUser user = findUser(userId);
        preventSelfManagement(user, actingUsername);
        user.setRoles(EnumSet.of(role));
        user.setUpdatedAt(Instant.now());
        users.save(user);
        invalidateSessions(user.getUsername());
    }

    public void setEnabled(String userId, boolean enabled, String actingUsername) {
        AppUser user = findUser(userId);
        preventSelfManagement(user, actingUsername);
        user.setEnabled(enabled);
        user.setUpdatedAt(Instant.now());
        users.save(user);
        if (!enabled) {
            invalidateSessions(user.getUsername());
        }
    }

    public void deleteUser(String userId, String actingUsername) {
        AppUser user = findUser(userId);
        preventSelfManagement(user, actingUsername);
        users.delete(user);
        invalidateSessions(user.getUsername());
    }

    private AppUser findUser(String userId) {
        return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private void preventSelfManagement(AppUser user, String actingUsername) {
        if (user.getUsername().equals(actingUsername)) {
            throw new IllegalStateException("Não é permitido alterar a própria conta de administrador.");
        }
    }

    private void invalidateSessions(String username) {
        // Mudança de perfil, bloqueio ou exclusão revoga as sessões já abertas.
        for (Map.Entry<String, ? extends Session> session : sessions.findByPrincipalName(username).entrySet()) {
            sessions.deleteById(session.getKey());
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
