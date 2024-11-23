package com.plaything.api.domain.repository.entity.log;

import com.plaything.api.domain.repository.entity.common.BaseLogEntity;
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
public class AdViewLog extends BaseLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String adType;

    @Column(nullable = false)
    private int viewDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
