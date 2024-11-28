package com.plaything.api.domain.repository.repo.filter;

import com.plaything.api.domain.repository.entity.filter.FilterWords;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterWordsRepository extends JpaRepository<FilterWords, Long> {
}
