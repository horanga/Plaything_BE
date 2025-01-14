package com.plaything.api.domain.repository.entity.profile;

import com.plaything.api.domain.repository.entity.monitor.ProfileImageRegistration;
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
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fileName;

    private boolean isMainPhoto;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "profile_image_registration_id")
    private ProfileImageRegistration profileImageRegistration;

    public void setProfileImageRegistration(ProfileImageRegistration profileImageRegistration) {
        this.profileImageRegistration = profileImageRegistration;
    }

    public void cancelMainPhoto() {
        this.isMainPhoto = false;
    }

    public void setMainPhoto() {
        this.isMainPhoto = true;
    }
}
