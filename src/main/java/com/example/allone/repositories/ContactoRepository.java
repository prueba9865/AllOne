package com.example.allone.repositories;

import com.example.allone.models.Contacto;
import com.example.allone.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    List<Contacto> findByContactoIdAndAceptadoFalse(Long contactoId);

    List<Contacto> findByUsuarioAndAceptadoTrue(Usuario usuario);

    boolean existsByUsuarioAndContacto(Usuario usuarioOrigen, Usuario usuarioDestino);

    List<Contacto> findByUsuarioId(Long userId);

    List<Contacto> findByUsuarioIdAndAceptadoTrue(Long id);

    List<Contacto> findByContactoIdAndAceptadoTrue(Long id);
}
