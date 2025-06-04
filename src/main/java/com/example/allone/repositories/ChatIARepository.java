package com.example.allone.repositories;

import com.example.allone.models.ChatIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatIARepository extends JpaRepository<ChatIA, Long> {
}
