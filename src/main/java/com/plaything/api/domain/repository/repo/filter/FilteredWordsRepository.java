package com.plaything.api.domain.repository.repo.filter;

import com.plaything.api.domain.repository.entity.filter.FilteredWordStat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FilteredWordsRepository extends JpaRepository<FilteredWordStat, Long> {

  @Query("""
      SELECT f FROM FilteredWordStat f
                  ORDER BY f.count DESC
                              LIMIT 10
      """)
  List<FilteredWordStat> getTopFilteredWords();
}
