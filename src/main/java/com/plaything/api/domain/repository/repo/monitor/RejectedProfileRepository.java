package com.plaything.api.domain.repository.repo.monitor;

import com.plaything.api.domain.repository.entity.monitor.RejectedProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectedProfileRepository extends JpaRepository<RejectedProfile, Long> {

}
