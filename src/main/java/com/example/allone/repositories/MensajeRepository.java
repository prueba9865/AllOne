package com.example.allone.repositories;

import com.example.allone.models.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    //List<Mensaje> findByChatIdOrderByCreatedAtAsc(Long chatId);
    @Query("SELECT m FROM Mensaje m WHERE " +
            "(m.usuario.id = :idUsuario1 AND m.contacto.id = :idUsuario2) OR " +
            "(m.usuario.id = :idUsuario2 AND m.contacto.id = :idUsuario1) " +
            "ORDER BY m.createdAt ASC")
    List<Mensaje> findConversacionBetweenUsers(
            @Param("idUsuario1") Long idUsuario1,
            @Param("idUsuario2") Long idUsuario2);
}
