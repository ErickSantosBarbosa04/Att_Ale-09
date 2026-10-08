package br.com.jurishome.auth.user;

import java.time.Instant;
import java.util.Set;

public record AdminUserView(String id, String displayName, String username, String email,
                            Set<Role> roles, boolean enabled, Instant createdAt) {
    public static AdminUserView from(AppUser user) {
        return new AdminUserView(user.getId(), user.getDisplayName(), user.getUsername(),
            user.getEmail(), Set.copyOf(user.getRoles()), user.isEnabled(), user.getCreatedAt());
    }
}
