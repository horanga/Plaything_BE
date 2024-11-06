package com.plaything.api.domain.repository.entity.user;

import com.plaything.api.domain.repository.entity.user.profile.Profile;
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

    private String url;

    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Profile profile;
}
