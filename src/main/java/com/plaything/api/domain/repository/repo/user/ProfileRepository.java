package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.profile.Profile;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("""
               SELECT DISTINCT p FROM Profile p
                                             INNER JOIN p.profileImages pi
                                             INNER JOIN fetch p.user u
                                             WHERE u.loginId IN :loginids
                                             AND pi.isMainPhoto = true
            """)
    List<Profile> findByLoginId(@Param("loginids") List<String> loginids);

    Profile findByNickName(String name);
}
