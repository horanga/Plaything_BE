package com.plaything.api.domain.repository.entity.profile;

import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
    @Index(name = "idx_trait", columnList = "trait")
}
)
@Entity
public class PersonalityTrait {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PersonalityTraitConstant trait;

  @Column(nullable = false)
  private boolean isPrimaryTrait;

  public PersonalityTrait checkPrimaryTrait(PersonalityTraitConstant primaryTrait) {

    if (this.trait.equals(primaryTrait)) {
      this.isPrimaryTrait = true;
    }

    return this;
  }

}
