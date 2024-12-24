package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalityTraitRepository extends JpaRepository<PersonalityTrait, Long> {
}
