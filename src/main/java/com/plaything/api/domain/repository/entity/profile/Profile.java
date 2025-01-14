package com.plaything.api.domain.repository.entity.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.profile.constants.Gender;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.profile.model.request.ProfileImageRequest;
import com.plaything.api.domain.repository.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.plaything.api.domain.profile.constants.Constants.LIMIT_OF_SIZE_PERSONALITY_TRAIT;
import static com.plaything.api.domain.profile.constants.Constants.LIMIT_OF_SIZE_RELATIONSHIP_PREFERENCE;

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

    @OneToOne(mappedBy = "profile")
    private User user;

    @Column
    private boolean isDeleted;

    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private final List<PersonalityTrait> personalityTrait = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private final List<RelationshipPreference> relationshipPreference = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private final List<ProfileImage> profileImages = new ArrayList<>();

    public void update(
            String nickName,
            String introduction,
            Gender gender,
            PrimaryRole primaryRole,
            List<Integer> personalityTraitToRemove,
            List<PersonalityTrait> personalityNewTrait,
            List<Integer> relationshipPreferenceToRemove,
            List<RelationshipPreference> relationshipNewPreference
    ) {

        this.primaryRole = primaryRole;

        if (personalityTraitToRemove != null && !personalityTraitToRemove.isEmpty()) {
            for (Integer i : personalityTraitToRemove) {
                //Integer를 바로 넣으면 remove(Object o)가 호출되면서 객체를 인자로 받는 메서드가 사용됨
                personalityTrait.remove(i.intValue());
            }
        }

        if (personalityNewTrait != null && !personalityNewTrait.isEmpty()) {
            int size = personalityNewTrait.size();
            if (personalityTrait.size() + personalityNewTrait.size() > LIMIT_OF_SIZE_PERSONALITY_TRAIT) {
                size = LIMIT_OF_SIZE_PERSONALITY_TRAIT - personalityTrait.size();
            }
            for (int i = 0; i < size; i++) {
                personalityTrait.add(personalityNewTrait.get(i));
            }
        }

        boolean hasPrimaryTrait = personalityTrait.stream().anyMatch(PersonalityTrait::isPrimaryTrait);
        long countOfPrimaryTrait = personalityTrait.stream().filter(PersonalityTrait::isPrimaryTrait).count();

        if (countOfPrimaryTrait > 1) {
            throw new CustomException(ErrorCode.PRIMARY_TRAIT_ALREADY_EXIST);
        }

        if (!hasPrimaryTrait) {
            throw new CustomException(ErrorCode.TRAITS_NOT_INCLUDE_PRIMARY);
        }

        if (relationshipPreferenceToRemove != null && !relationshipPreferenceToRemove.isEmpty()) {
            for (Integer i : relationshipPreferenceToRemove) {
                relationshipPreference.remove(i.intValue());
            }
        }

        if (relationshipNewPreference != null && !relationshipNewPreference.isEmpty()) {
            if (relationshipPreference.size() < LIMIT_OF_SIZE_RELATIONSHIP_PREFERENCE) {
                relationshipPreference.addAll(relationshipNewPreference);
            }
        }

        this.nickName = nickName;
        this.introduction = introduction;
        this.gender = gender;
        this.profileStatus = ProfileStatus.UPDATED;

    }

    public List<ProfileImage> updateProfilePictures(List<String> picturesToRemove, List<ProfileImage> newProfileImages, Integer mainPhotoIndex, boolean shouldCancelMainPhoto) {

        if (picturesToRemove != null && !picturesToRemove.isEmpty()) {
            for (String name : picturesToRemove) {
                this.profileImages.removeIf(images ->
                        images.getFileName().equals(name));
            }
        }

        if (shouldCancelMainPhoto) {
            this.profileImages.stream().filter(ProfileImage::isMainPhoto).forEach(ProfileImage::cancelMainPhoto);
        }

        this.profileImages.addAll(newProfileImages);
        if (mainPhotoIndex != null && shouldCancelMainPhoto) {

            this.profileImages.stream()
                    .filter(ProfileImage::isMainPhoto)
                    .forEach(ProfileImage::cancelMainPhoto);
            this.profileImages.get(mainPhotoIndex).setMainPhoto();
        }
        return this.profileImages;
    }

    public void validateUpdateRequest(
            List<String> picturesToRemove,
            List<ProfileImageRequest> newProfileImages,
            boolean shouldCancelMainPhoto) {


        long countOfMainImagesOfNewImages = newProfileImages.stream()
                .filter(ProfileImageRequest::isMainPhoto)
                .count();

        if (countOfMainImagesOfNewImages > 1) {
            throw new CustomException(ErrorCode.MAIN_IMAGE_COUNT_EXCEEDED);
        }


        int newImagesSize = (newProfileImages != null) ? newProfileImages.size() : 0;
        int removeSize = (picturesToRemove != null) ? picturesToRemove.size() : 0;
        int size = this.profileImages.size() + newImagesSize - removeSize;

        if (size < 1) {
            throw new CustomException(ErrorCode.IMAGE_REQUIRED);
        }
        if (size > 3) {
            throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
        }

        boolean hasMainPhoto =
                newProfileImages.stream()
                        .anyMatch(ProfileImageRequest::isMainPhoto);

        if (!hasMainPhoto && shouldCancelMainPhoto) {
            throw new CustomException(ErrorCode.MAIN_IMAGE_REQUIRED);
        }

        if (hasMainPhoto && !shouldCancelMainPhoto) {
            throw new CustomException(ErrorCode.MAIN_IMAGE_COUNT_EXCEEDED);
        }
    }

    public void addPersonalityTrait(List<PersonalityTrait> personalityTrait) {
        this.personalityTrait.addAll(personalityTrait);
    }

    public void addRelationshipPreference(List<RelationshipPreference> relationshipPreference) {
        this.relationshipPreference.addAll(relationshipPreference);
    }

    public List<ProfileImage> addProfileImages(List<ProfileImage> profileImages) {
        this.profileImages.addAll(profileImages);
        return profileImages;
    }

    public void addProfileImage(ProfileImage profileImage) {
        this.profileImages.add(profileImage);
    }

    public String getLoginId() {
        return this.user.getLoginId();
    }

    public PersonalityTraitConstant getPrimaryTrait() {
        return this.personalityTrait.stream().filter(PersonalityTrait::isPrimaryTrait)
                .map(PersonalityTrait::getTrait)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_PRIMARY_TRAIT));
    }

    public void setPrivate() {
        this.isPrivate = true;
    }

    public void setPublic() {
        this.isPrivate = false;
    }

    public int calculateAge() {
        //만 나이로 표시
        return LocalDate.now().getYear() - this.birthDate.getYear();
    }

    public void setBaned() {
        this.isBaned = true;
    }

    public void setProfileStatusRejected() {
        this.profileStatus = ProfileStatus.REJECTED;
    }

    public boolean isProfileImagesEmpty() {
        return this.profileImages.isEmpty();
    }

    public boolean isSwitch() {
        return this.primaryRole.equals(PrimaryRole.SWITCH);
    }

    public boolean isETC() {
        return this.primaryRole.equals(PrimaryRole.ETC);
    }

    public String getMainPhotoFileName() {
        ProfileImage profileImage = this.profileImages.stream()
                .filter(ProfileImage::isMainPhoto)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_MAIN_PHOTO));

        return profileImage.getFileName();
    }

    public String getPrimaryRoleAsString() {
        return primaryRole.getPrimaryRole(this.gender);
    }

    public PrimaryRole getPrimaryRole() {
        return primaryRole;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void updateMainPhoto(Integer indexOfMainImage) {
        this.profileImages.stream().filter(ProfileImage::isMainPhoto).forEach(ProfileImage::cancelMainPhoto);
        this.profileImages.get(indexOfMainImage).setMainPhoto();

    }
}
