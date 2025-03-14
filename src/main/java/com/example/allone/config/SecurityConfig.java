package com.example.allone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(this.userDetailsService); // Usa el atributo de clase
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:63342", "https://tufrontend.com")); // Permitir orígenes específicos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With")); // Encabezados permitidos
        config.setAllowCredentials(true); // Permitir credenciales

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Aplicar configuración a todas las rutas
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());  // Crea el filtro CORS con la configuración
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())   // Desactivamos CSRF para APIs RESTful con tokens
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Usamos tokens JWT sin sesión en el servidor
                .authorizeHttpRequests(auth -> auth
                        //.requestMatchers(HttpMethod.POST, "/clientes").authenticated()
                        /*.requestMatchers(HttpMethod.PUT, "/clientes/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/clientes/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/mesas").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/mesas/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/mesas/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/reservas").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reservas/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/auth/asignarRol/*").authenticated()*/
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // Filtro JWT para autenticación

        return http.build();
    }
}

