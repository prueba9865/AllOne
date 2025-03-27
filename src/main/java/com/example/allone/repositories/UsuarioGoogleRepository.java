package com.example.allone.repositories;

import com.example.allone.models.Usuario;
import com.example.allone.models.UsuarioGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioGoogleRepository extends JpaRepository<UsuarioGoogle, Long> {
    Optional<Usuario> findByUsername(String username);
}
