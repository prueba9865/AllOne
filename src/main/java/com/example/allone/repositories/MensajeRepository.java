package com.example.allone.repositories;

import com.example.allone.models.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    //List<Mensaje> findByChatIdOrderByCreatedAtAsc(Long chatId);
    List<Mensaje> findByUsuarioIdOrderByCreatedAtAsc(Long usuarioId);;
}
