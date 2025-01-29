package com.plaything.api.domain.repository.entity.profile;

import com.plaything.api.domain.repository.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProfileHidePreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "targer_user_id", nullable = false)
  private User targetUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "setter_user_id", nullable = false)
  private User settingUser;

  @Column
  private LocalDate createAt;
}
