package com.plaything.api.domain.repository.entity.user.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.user.constants.Gender;

import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.ProfileStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

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

    @Column
    private boolean isBaned;

    @Column(nullable = false, length = 12, unique = true)
    private String nickName;

    @Column(nullable = false, length = 30)
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
    @Enumerated(EnumType.STRING)
    private ProfileStatus profileStatus;

    @Column(nullable = false)
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private final List<PersonalityTrait> personalityTrait = new ArrayList<>();

    @Column(nullable = false)
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private final List<RelationshipPreference> relationshipPreference = new ArrayList<>();

    @BatchSize(size = 100)
    @Column(nullable = false)
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private final List<ProfileImage> profileImages = new ArrayList<>();

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
    }

    public void addPersonalityTrait(List<PersonalityTrait> personalityTrait) {
        this.personalityTrait.addAll(personalityTrait);
    }

    public void addRelationshipPreference(List<RelationshipPreference> relationshipPreference) {
        this.relationshipPreference.addAll(relationshipPreference);
    }

    public void addProfileImages(List<ProfileImage> profileImages) {
        this.profileImages.addAll(profileImages);
    }

    public PersonalityTraitConstant getPrimaryTrait(){
       return this.personalityTrait.stream().filter(PersonalityTrait::isPrimaryTrait)
               .map(PersonalityTrait::getTrait)
               .findFirst()
               .orElseThrow(()->new CustomException(ErrorCode.NOT_EXIST_PRIMARY_TRAIT));
    }

    public void setPrivate(){
        this.isPrivate = true;
    }

    public void setPublic(){
        this.isPrivate = false;
    }

    public int calculateAge(){
        //만 나이로 표시
       return LocalDate.now().getYear()- this.birthDate.getYear();
    }

    public void setBaned(){
        this.isBaned = true;
    }

    public void setProfileStatusRejected(){
        this.profileStatus = ProfileStatus.REJECTED;
    }

    public boolean isProfileImagesEmpty(){
        return this.profileImages.isEmpty();
    }
}
