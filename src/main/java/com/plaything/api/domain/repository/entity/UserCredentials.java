package com.plaything.api.domain.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserCredentials {

    @Id
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private String hashedPassowrd;
}
