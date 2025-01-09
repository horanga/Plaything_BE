package com.plaything.api.domain.repository.repo.pay;

import com.plaything.api.domain.repository.entity.pay.PointKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointKeyRepository extends JpaRepository<PointKey, Long> {

    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN p.status = 'EARN' THEN 1
                    WHEN p.status = 'USED' THEN -1
                ELSE 0
                END), 0)
            FROM PointKey p
            inner join User u on u.loginId = :loginId
            WHERE  p.user.id = u.id
                AND p.isValidKey = true
            """)
    long countAvailablePointKey(String loginId);

    boolean existsByTransactionId(String transactionId);
}
