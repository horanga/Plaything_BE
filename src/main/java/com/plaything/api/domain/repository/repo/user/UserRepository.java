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

    //pattern, user를 기준으로 네임값을 가져온다. locate -->데이터에 있는 name값이 패턴에 몇번째(인덱스)에 위치하는지 -->0보다 크면 패턴 만족
    @Query("Select u.loginId from User u where LOCATE(LOWER(:pattern), lower(u.loginId)) >0 and u.loginId !=:user")
    List<String> findLoginIdByLoginIdMatch(@Param("pattern") String pattern, @Param("user") String user);

    List<User> findUserByProfile_PersonalityTrait_trait(PersonalityTraitConstant trait);

}
