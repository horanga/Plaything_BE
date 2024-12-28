package com.plaything.api.domain.repository.repo.matching;

import com.plaything.api.domain.repository.entity.matching.Matching;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query("""
            SELECT m FROM Matching m 
            WHERE (m.senderLoginId =:loginId OR m.receiverLoginId =:loginId)
            AND (m.isMatched = true AND m.isOvered = false)
            """)
    List<Matching> findSuccessAndNotOverMatching(@Param("loginId") String loginId);

    @Query("""
            SELECT m FROM Matching m
            WHERE (m.senderLoginId =:loginId OR m.receiverLoginId =:loginId)
            AND m.isMatched =false
            """)
    List<Matching> findMatchingCandidate(@Param("loginId") String loginId);

    Optional<Matching> findBySenderLoginIdAndReceiverLoginId(String senderLoginId, String receiverLoginId);

}
