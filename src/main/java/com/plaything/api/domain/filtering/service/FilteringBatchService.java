package com.plaything.api.domain.filtering.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FilteringBatchService {

  @Autowired
  private final JdbcTemplate jdbcTemplate;

  public void insertAllFilteredWords(Map<String, Integer> map) {

    String sql = """
            INSERT INTO filtered_words (word, count)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE
            count = count + VALUES(count)
        """;

    List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setString(1, entries.get(i).getKey());
        ps.setInt(2, entries.get(i).getValue());
      }

      @Override
      public int getBatchSize() {
        return entries.size();
      }
    });
  }
}
