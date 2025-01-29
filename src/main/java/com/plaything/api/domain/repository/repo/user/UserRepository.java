package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginId(String loginId);

  @Query("SELECT u FROM User u JOIN FETCH u.credentials WHERE u.loginId = :loginId")
  Optional<User> findByLoginIdWithCredentials(String loginId);
}
