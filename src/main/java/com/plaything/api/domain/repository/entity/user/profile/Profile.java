package com.plaything.api.domain.repository.entity.user.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.plaything.api.domain.user.constants.Gender;

import com.plaything.api.domain.user.constants.PrimaryRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private boolean isPrivate;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String introduction;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrimaryRole primaryRole;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Column(nullable = false)
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<PersonalityTrait> personalityTrait = new ArrayList<>();

    @Column(nullable = false)
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<RelationshipPreference> relationshipPreference= new ArrayList<>();

    public void update(
            String nickName,
            String introduction,
            Gender gender,
            PrimaryRole primaryRole,
            List<PersonalityTrait> personalityTrait,
            List<RelationshipPreference> relationshipPreference
    ){
        this.nickName=nickName;
        this.introduction = introduction;
        this.gender = gender;
        this.primaryRole = primaryRole;

        //TODO 교체방식으로 변경
        this.personalityTrait = personalityTrait;
        this.relationshipPreference = relationshipPreference;
    }

    public void setPersonalityTrait(List<PersonalityTrait> personalityTrait) {
        this.personalityTrait = personalityTrait;
    }

    public void setRelationshipPreference(List<RelationshipPreference> relationshipPreference) {
        this.relationshipPreference = relationshipPreference;
    }

    public void setPrivate(){
        this.isPrivate = true;
    }


    public void setPublic(){
        this.isPrivate = false;
    }
}
