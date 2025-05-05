package com.example.allone.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*Usuario autenticado: {sub=116659339885663170497, name=THOR - Brawl Stars, given_name=THOR, family_name=- Brawl Stars,
picture=https://lh3.googleusercontent.com/a/ACg8ocKe-U0E6b0u96Ncc4rSNOsSHPp-1jtBg2B_eJ0nxsrTS4vNRm0=s96-c,
email=thorgamer144@gmail.com, email_verified=true}*/

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioGoogle implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Introduce un email válido")
    @NotBlank(message = "El email no puede estar en blanco")
    private String email;

    @Column(unique = true)
    @NotBlank(message = "El usuario no puede estar en blanco")
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,20}$", message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String username;

    private String avatar;
    private String rolUser;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP(0)")  // Asegura que se almacene sin microsegundos
    private LocalDateTime createdAt;

    /*@OneToMany(mappedBy = "usuarioGoogle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajes = new ArrayList<>();*/

    @PrePersist
    public void prePersist() {
        this.rolUser = "USER";
        this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS); // Truncado a segundos
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rolUser));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

