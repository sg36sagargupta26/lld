package com.lld.practice.repository;

import com.lld.practice.entity.UserRateLimit;
import com.lld.practice.modal.UserRateLimitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRateLimitRepository extends JpaRepository<UserRateLimit, UserRateLimitId> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO user_rate_limits (user_id, request_at)
        SELECT :userId, CURRENT_TIMESTAMP
        WHERE (
            SELECT COUNT(*)
            FROM user_rate_limits
            WHERE user_id = :userId
              AND request_at > (CURRENT_TIMESTAMP - interval '1 minute')
        ) < :maxRequests
        """, nativeQuery = true)
    public int tryInsertRequest(String userId, int maxRequests);

    default boolean isNotAllowed(String userId, int maxRequests) {
        return tryInsertRequest(userId, maxRequests) == 0;
    }
}
