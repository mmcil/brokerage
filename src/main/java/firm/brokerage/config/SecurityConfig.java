package firm.brokerage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean for encoding customer passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Basic security configuration
     * For now, we'll use basic HTTP authentication for admin endpoints
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                        .requestMatchers("/api/auth/**").permitAll()   // Allow authentication endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin endpoints require ADMIN role
                        .requestMatchers("/api/**").authenticated()     // All other API endpoints require authentication
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Brokerage API")) // Basic auth for admin
                .headers(headers -> headers.frameOptions().disable()); // Allow H2 console frames

        return http.build();
    }
}
