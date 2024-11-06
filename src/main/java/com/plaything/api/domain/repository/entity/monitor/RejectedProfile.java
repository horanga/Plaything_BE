package com.plaything.api.domain.repository.entity.monitor;

import com.plaything.api.domain.repository.entity.user.User;
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
public class RejectedProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String introduction;

    @Column(nullable = false)
    private String rejectedReason;

    @Column(nullable = false)
    private long profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
