package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.profile.RelationshipPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationshipRepository extends JpaRepository<RelationshipPreference, Long> {

}
