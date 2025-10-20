package com.main.projetstage.configs;

import com.main.projetstage.services.UtilisateurDetailsServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Recommended password encoder
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService utilisateurDetailsService;
    private final SuccessHandlerConfig successHandlerConfig; // Use the actual class name

    public SecurityConfig(UserDetailsService utilisateurDetailsService,SuccessHandlerConfig successHandlerConfig) {
        this.utilisateurDetailsService = utilisateurDetailsService;
        this.successHandlerConfig = successHandlerConfig;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(utilisateurDetailsService); // Your custom service
        authProvider.setPasswordEncoder(passwordEncoder());          // Your chosen encoder
        return authProvider;
    }

    // --- 3. Security Filter Chain (Main Configuration) ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for now (consider enabling for production)
                .authorizeHttpRequests(authorize -> authorize
                        // Allow unauthenticated access to your custom login and register pages,
                        // and any static assets (CSS, JS, images)
                         .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/comptable/**").hasRole("COMPTABLE")
                        .requestMatchers("/bordereaux/**").hasRole("COMPTABLE")
                        .requestMatchers("/ordonnateur/**").hasRole("ORDONNATEUR")
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")             // Specify your custom login page URL
                        .loginProcessingUrl("/authenticate") // URL where your login form POSTs
                        .successHandler(successHandlerConfig)
                        .failureUrl("/login?error=true") // Redirect after failed login
                        .permitAll()                     // Allow access to the login form for all
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")            // URL to trigger logout
                        .logoutSuccessUrl("/login?logout=true") // Redirect after successful logout
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied") // Optional: custom access denied page
                );

        return http.build();
    }
}
