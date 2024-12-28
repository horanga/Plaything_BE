package com.plaything.api.util;

import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.constant.PointStatus;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.matching.service.MatchingFacadeV1;
import com.plaything.api.domain.repository.entity.pay.PointKey;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.plaything.api.domain.user.constants.Gender.M;

@Component
public class UserGenerator {

    private final AuthServiceV1 authServiceV1;
    private final ProfileFacadeV1 profileFacadeV1;
    private final ProfileRepository profileRepository;
    private final MatchingFacadeV1 matchingFacadeV1;
    private PointKeyRepository pointKeyRepository;
    private UserRepository userRepository;

    public UserGenerator(AuthServiceV1 authServiceV1, ProfileFacadeV1 profileFacadeV1, ProfileRepository profileRepository, MatchingFacadeV1 matchingFacadeV1, PointKeyRepository pointKeyRepository, UserRepository userRepository) {
        this.authServiceV1 = authServiceV1;
        this.profileFacadeV1 = profileFacadeV1;
        this.profileRepository = profileRepository;
        this.matchingFacadeV1 = matchingFacadeV1;
        this.pointKeyRepository = pointKeyRepository;
        this.userRepository = userRepository;
    }

    public void createPointKey(String loginId, int num) {
        User user = userRepository.findByLoginId(loginId).get();
        for (int i = 0; i < num; i++) {
            PointKey build = PointKey.builder().status(PointStatus.EARN).user(user).isValidKey(true).build();
            pointKeyRepository.save(build);
        }
    }

    public void generate(String loginId, String password, String fckToken, String nickName) {

        CreateUserRequest request = new CreateUserRequest(loginId, password, fckToken);
        authServiceV1.creatUser(request);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                nickName, "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, loginId);
    }

    public void generateWithRole(String loginId, String password, String fckToken, String nickName, PrimaryRole primaryRole, PersonalityTraitConstant personalityTraitConstant) {

        CreateUserRequest request = new CreateUserRequest(loginId, password, fckToken);
        authServiceV1.creatUser(request);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                nickName, "hi", M, primaryRole, List.of(personalityTraitConstant), personalityTraitConstant, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, loginId);
    }

    public void addImages(String nickName, String fileName) {
        Profile byNickName = profileRepository.findByNickName(nickName);
        byNickName.addProfileImages(List.of(ProfileImage.builder().profile(byNickName).fileName(fileName).isMainPhoto(true).build()));
    }

    public void requestMatching(String loginId, String password, String partnerLoginId) {
        LocalDate now = LocalDate.now();
        LoginRequest loginRequest = new LoginRequest(loginId, password);
        authServiceV1.login(loginRequest, now, String.valueOf(UUID.randomUUID()));
        MatchingRequest matchingRequest = new MatchingRequest(partnerLoginId);
        matchingFacadeV1.createMatching(loginId, matchingRequest, String.valueOf(UUID.randomUUID()));
    }

    public void createMatching(String loginId, String password, String loginId2, String password2) {
        LocalDate now = LocalDate.now();
        LoginRequest loginRequest = new LoginRequest(loginId, password);
        authServiceV1.login(loginRequest, now, String.valueOf(UUID.randomUUID()));

        LoginRequest loginRequest2 = new LoginRequest(loginId2, password2);
        authServiceV1.login(loginRequest2, now, String.valueOf(UUID.randomUUID()));
        MatchingRequest matchingRequest = new MatchingRequest(loginId2);
        matchingFacadeV1.createMatching(loginId, matchingRequest, String.valueOf(UUID.randomUUID()));
        matchingFacadeV1.acceptMatching(loginId2, new MatchingRequest(loginId), String.valueOf(UUID.randomUUID()));
    }
}
