package com.plaything.api.domain.repository.entity.user.profile;

import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

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
