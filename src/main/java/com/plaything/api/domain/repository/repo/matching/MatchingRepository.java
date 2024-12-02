package com.plaything.api.domain.repository.repo.matching;

import com.plaything.api.domain.repository.entity.matching.Matching;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query("""
            SELECT m FROM Matching m 
            WHERE (m.senderNickname =:nickname OR m.receiverNickname =:nickname)
            AND (m.isMatched = true AND m.isOvered = false)
            """)
    List<Matching> findSuccessAndNotOveMatching(@Param("nickname") String nickname);

}
