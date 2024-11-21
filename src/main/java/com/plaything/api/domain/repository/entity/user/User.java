package com.plaything.api.domain.repository.entity.user;

import com.plaything.api.domain.repository.entity.common.BaseEntity;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.user.constants.ProfileStatus;
import com.plaything.api.domain.user.constants.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "User_credentials_id", referencedColumnName = "id")
    private UserCredentials credentials;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Profile profile;

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setCredentials(UserCredentials credentials) {
        this.credentials = credentials;
    }

    public boolean isProfileEmpty() {
        return this.profile == null;
    }

    public boolean isPreviousProfileRejected() {
        return this.profile.getProfileStatus().equals(ProfileStatus.REJECTED);
    }
}
