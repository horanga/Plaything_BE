package com.plaything.api.domain.repository.entity.user.profile;

import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
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
public class RelationshipPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Profile profile;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipPreferenceConstant relationshipPreference;
}
