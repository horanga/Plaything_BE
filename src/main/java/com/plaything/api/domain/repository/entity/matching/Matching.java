package com.plaything.api.domain.repository.entity.matching;

import com.plaything.api.domain.repository.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_senderLoginId", columnList = "senderLoginId"),
        @Index(name = "idx_receiverLoginId", columnList = "receiverLoginId")
})
@Entity
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String senderLoginId;

    @Column(nullable = false)
    private String receiverLoginId;

    @Column
    private boolean isMatched;

    @Column
    private boolean isOvered;

    public void acceptMatching() {
        this.isMatched = true;
    }
}
