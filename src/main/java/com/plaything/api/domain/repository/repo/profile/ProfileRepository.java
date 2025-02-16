package com.plaything.api.domain.repository.repo.profile;

import com.plaything.api.domain.repository.entity.profile.Profile;
import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

  @Query("""
         SELECT DISTINCT p FROM Profile p
                                       INNER JOIN fetch p.profileImages pi
                                       INNER JOIN fetch p.user u
                                       WHERE u.loginId IN :loginids
                                       AND pi.isMainPhoto = true
      """)
  List<Profile> findByLoginId(@Param("loginids") List<String> loginids);

  Profile findByNickName(String name);

  boolean existsByNickName(String nickName);

  Profile findByUser_LoginId(String loginId);
}
