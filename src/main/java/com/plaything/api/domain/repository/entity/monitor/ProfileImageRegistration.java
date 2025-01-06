package com.plaything.api.domain.repository.entity.monitor;

import com.plaything.api.domain.repository.entity.profile.ProfileImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProfileImageRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "profile_image_id")
    private ProfileImage profileImage;
}
