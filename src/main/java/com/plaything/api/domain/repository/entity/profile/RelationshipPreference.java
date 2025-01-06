package com.plaything.api.domain.repository.entity.profile;

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
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipPreferenceConstant relationshipPreference;
}
