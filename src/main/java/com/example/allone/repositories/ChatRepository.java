package com.example.allone.repositories;

import com.example.allone.models.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
      select c from Chat c
      join c.participantes u1
      join c.participantes u2
      where c.tipo = 'individual'
        and u1.id = :userA
        and u2.id = :userB
    """)
    Optional<Chat> findIndividualChatBetween(@Param("userA") Long userA,
                                             @Param("userB") Long userB);
}
