package com.plaything.api.domain.repository.entity.user;

import com.plaything.api.domain.repository.entity.common.BaseEntity;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.profile.constants.Role;
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

    @Column
    private boolean isDeleted;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "User_credentials_id", referencedColumnName = "id")
    private UserCredentials credentials;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Profile profile;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "violation_stats_id")
    private UserViolationStats violationStats;

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setCredentials(UserCredentials credentials) {
        this.credentials = credentials;
    }

    public void setViolationStats(UserViolationStats violationStats) {
        this.violationStats = violationStats;
    }

    public boolean isProfileEmpty() {
        return this.profile == null;
    }

    public boolean isPreviousProfileRejected() {
        return this.profile.getProfileStatus().equals(ProfileStatus.REJECTED);
    }

    public String getNickname() {
        return this.profile.getNickName();
    }

    public void delete() {
        this.isDeleted = true;
        profile.delete();
    }
}
