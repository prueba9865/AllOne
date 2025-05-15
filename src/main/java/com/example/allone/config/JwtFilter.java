package com.example.allone.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extrae el token JWT de la cabecera Authoritation de la petición HTTP
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = this.extractToken(request);

            if (token != null) {
                // Validar el token antes de procesarlo
                if (!this.tokenProvider.isValidToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
                    return;
                }

                String username = this.tokenProvider.getUsernameFromToken(token);

                // Verificar que el username no sea nulo o vacío
                if (username == null || username.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no contiene información de usuario válida");
                    return;
                }

                // UserDetails representa al usuario
                UserDetails user = this.userDetailsService.loadUserByUsername(username);

                // Información sobre el usuario que se acaba de autenticar
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null, // No necesitamos las credenciales después de autenticar
                        user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            // Reenviamos la petición a los siguientes filtros
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // Manejar específicamente errores JWT
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticación: " + e.getMessage());
        } catch (Exception e) {
            // Manejar otros errores inesperados
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Excluir endpoints públicos
        return request.getServletPath().startsWith("/api/v1/auth/") ||
                request.getServletPath().equals("/decode-jwt") ||
                request.getServletPath().startsWith("/uploads/avatars/");
    }

    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}