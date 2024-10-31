package com.plaything.api.domain.repository.entity.user.profile;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
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
    @JoinColumn(name = "profile_images_id")
    private ProfileImage profileImage;
}
