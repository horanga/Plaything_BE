package com.plaything.api.domain.repository.entity.filter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FilterWords {

  @Column(unique = true)
  public String word;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
}