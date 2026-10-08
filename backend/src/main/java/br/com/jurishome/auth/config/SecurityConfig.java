package br.com.jurishome.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // A proteção CSRF padrão do Spring Security continua ativa para formulários POST.
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/cadastro", "/login", "/403", "/error",
                    "/css/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/advogado/**").hasAnyRole("ADVOGADO", "ADMIN")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/painel").authenticated()
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/painel", true)
                .failureUrl("/login?erro")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JURISSESSION")
                .permitAll()
            )
            .sessionManagement(session -> session
                // Troca o identificador após o login para reduzir risco de session fixation.
                .sessionFixation(fixation -> fixation.changeSessionId())
            )
            .exceptionHandling(errors -> errors.accessDeniedPage("/403"))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; style-src 'self'; img-src 'self' data:; " +
                    "form-action 'self'; frame-ancestors 'none'; base-uri 'self'; object-src 'none'"))
            );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // BCrypt é adaptativo; o custo pode ser calibrado ao ambiente de produção.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CookieSerializer sessionCookieSerializer(
        @Value("${app.security.cookie-secure:false}") boolean secureCookie) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JURISSESSION");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(secureCookie);
        serializer.setSameSite("Lax");
        return serializer;
    }
}
