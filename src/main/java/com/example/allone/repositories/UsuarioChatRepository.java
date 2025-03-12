package com.example.allone.repositories;

import com.example.allone.models.UsuarioChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioChatRepository extends JpaRepository<UsuarioChat, Long> {
}
