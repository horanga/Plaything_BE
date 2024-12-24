package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    @Query("Select u.loginId from User u where LOCATE(LOWER(:pattern), lower(u.loginId)) >0 and u.loginId !=:user")
    List<String> findLoginIdByLoginIdMatch(@Param("pattern") String pattern, @Param("user") String user);

    List<User> findUserByProfile_PersonalityTrait_trait(PersonalityTraitConstant trait);

}
