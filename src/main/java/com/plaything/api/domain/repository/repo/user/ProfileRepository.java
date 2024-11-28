package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.profile.Profile;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("""
               SELECT DISTINCT p FROM Profile p
                                             LEFT JOIN FETCH p.profileImages pi
                                             WHERE p.nickName IN :nicknames
                                             AND pi.isMainPhoto = true
            """)
    List<Profile> findByNickNames(@Param("nicknames") List<String> nicknames);

    Profile findByNickName(String name);
}
