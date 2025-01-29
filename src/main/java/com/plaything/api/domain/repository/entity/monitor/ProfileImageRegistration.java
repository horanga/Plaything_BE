package com.plaything.api.domain.repository.entity.monitor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class ProfileImageRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String fileName;

  private long profileImageId;
}
