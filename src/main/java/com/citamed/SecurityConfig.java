package com.citamed;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    // Autenticacion y autorizacion por rol, siguiendo el esquema visto en clase.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                .requestMatchers("/", "/login", "/registro/**", "/acceso-denegado", "/css/**", "/webjars/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/paciente/**").hasRole("PACIENTE")
                .anyRequest().authenticated()
        );
        http.formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")
                .successHandler(exitoPorRol())
                .failureUrl("/login?error=true")
                .permitAll()
        );
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?salida=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );
        http.exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }

    // El destino tras el ingreso depende del rol autenticado.
    private AuthenticationSuccessHandler exitoPorRol() {
        return (request, response, authentication) -> {
            String destino = "/paciente";
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    destino = "/admin/dashboard";
                } else if (authority.getAuthority().equals("ROLE_DOCTOR")) {
                    destino = "/doctor/agenda";
                }
            }
            response.sendRedirect(request.getContextPath() + destino);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
