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
@Entity
public class PersonalityTrait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Profile profile;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PersonalityTraitConstant personalityTrait;

}
