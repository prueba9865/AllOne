package com.example.allone.repositories;

import com.example.allone.models.ChatIA;
import com.example.allone.models.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatIARepository extends JpaRepository<ChatIA, Long> {
}
