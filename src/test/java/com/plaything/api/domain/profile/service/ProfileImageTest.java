package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.profile.model.request.ProfileImageRequest;
import com.plaything.api.domain.profile.service.profileImage.ProfileImageServiceV1;
import com.plaything.api.domain.repository.entity.monitor.ProfileImageRegistration;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.monitor.ProfileImagesRegistrationRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import com.plaything.api.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
public class ProfileImageTest {

    @Autowired
    private ProfileImagesRegistrationRepository profileImagesRegistrationRepository;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private ProfileImageServiceV1 profileImageServiceV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {

        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (!keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }

        userGenerator.generate("fnel123", "123", "1", "연호");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false),
                        new SavedImage("사진3", false)),
                "fnel123");
    }

    @DisplayName("프로필 사진을 등록할 수 있다")
    @Test
    void test1() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false),
                        new SavedImage("사진3", false)),
                "fnel1234");

        Profile profile = profileFacadeV1.getProfileByLoginIdNotDTO("fnel1234");
        List<ProfileImage> profileImages = profile.getProfileImages();
        assertThat(profileImages).hasSize(3);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1", "사진2", "사진3");
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(true, false, false);
        List<ProfileImageRegistration> all = profileImagesRegistrationRepository.findAll();
        assertThat(all).hasSize(6);
        assertThat(all).extracting("fileName").containsExactly("사진1", "사진2", "사진3", "사진1", "사진2", "사진3");

    }

    @DisplayName("프로필 사진을 등록할 때 메인 사진을 필수로 골라야 한다")
    @Test
    void test2() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(List.of(new ProfileImageRequest(null, false)), "aa", "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진 중에 메인 사진을 골라야 합니다");
    }

    @DisplayName("프로필 사진을 등록할 때 사진을 하나 이상 등록해야 한다")
    @Test
    void test3() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(Collections.emptyList(), "aaD2", "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진을 필수로 등록해야 합니다");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(null, "ad12a", "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진을 필수로 등록해야 합니다");
    }

    @DisplayName("프로필 사진을 등록할 때 중복 요청을 거른다.")
    @Test
    void test4() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(Collections.emptyList(), "aDSADFa", "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진을 필수로 등록해야 합니다");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(null, "aDSADFa", "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("중복된 요청입니다");
    }


    @DisplayName("메인 사진으로 지정하지 않으면 프로필 사진 등록을 할 수 없다")
    @Test
    void test5() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(
                        List.of(new ProfileImageRequest(null, false)),
                        "aaDFA",
                        "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진 중에 메인 사진을 골라야 합니다");

    }

    @DisplayName("메인 사진은 하나만 지정할 수 있다")
    @Test
    void test6() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(
                        List.of(new ProfileImageRequest(null, true), new ProfileImageRequest(null, true)),
                        "aa2321",
                        "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("메인 사진은 하나만 골라야 합니다");

    }

    @DisplayName("프로필 사진은 세 장까지만 등록할 수 있다.")
    @Test
    void test7() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        assertThatThrownBy(() ->
                profileFacadeV1.registerImages(
                        List.of(
                                new ProfileImageRequest(null, true),
                                new ProfileImageRequest(null, false),
                                new ProfileImageRequest(null, false),
                                new ProfileImageRequest(null, false)),
                        "aaDASFDA",
                        "fnel1234"))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진은 최대 3개까지만 등록 가능합니다");

    }

    @DisplayName("프로필 사진을 등록하면 모니터링 자료가 생성된다")
    @Test
    void test8() {

        Profile profile = profileFacadeV1.getProfileByLoginIdNotDTO("fnel123");
        List<ProfileImageRegistration> list = profile.getProfileImages().stream().map(ProfileImage::getProfileImageRegistration).toList();
        assertThat(list).hasSize(3);
        assertThat(list).extracting("fileName").containsExactly("사진1", "사진2", "사진3");

    }


    @DisplayName("프로필 사진을 삭제하고 새로운 사진을 추가할 수 있다")
    @Test
    void test9() {

        Profile profile = profileRepository.findByUser_LoginId("fnel123");
        List<ProfileImage> profileImages = profile.getProfileImages();
        assertThat(profileImages).hasSize(3);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1", "사진2", "사진3");
        long id = profileImages.get(1).getId();
        ProfileImageRegistration registration = profileImagesRegistrationRepository.findByProfileImageId(id);
        assertThat(registration.getFileName()).isEqualTo("사진2");

        profileImageServiceV1.updateImages("fnel123", List.of("사진2", "사진3"), List.of(
                        new SavedImage("사진4", false),
                        new SavedImage("사진5", false)),
                null,
                false);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel123");
        List<ProfileImage> profileImages1 = profile1.getProfileImages();
        assertThat(profileImages1).hasSize(3);

        assertThat(profileImages).extracting("fileName").containsExactly("사진1", "사진4", "사진5");

        List<ProfileImageRegistration> list = profileImagesRegistrationRepository.findAll();
        assertThat(list).hasSize(3);
        ProfileImageRegistration registration2 = profileImagesRegistrationRepository.findByProfileImageId(id);
        assertThat(registration2).isNull();

    }

    @DisplayName("프로필 사진을 삭제할 수 있다(1)")
    @Test
    void test10() {

        profileFacadeV1.updateImages(
                "fnel123",
                Collections.emptyList(),
                "dsdfas",
                List.of("사진2", "사진3"),
                null,
                false);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel123");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(1);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1");

        List<ProfileImageRegistration> list = profileImagesRegistrationRepository.findAll();
        assertThat(list).hasSize(1);
        assertThat(list).extracting("fileName").containsExactly("사진1");
    }

    @DisplayName("프로필 사진을 삭제할 수 있다(2)")
    @Test
    void test11() {

        profileFacadeV1.updateImages(
                "fnel123",
                Collections.emptyList(),
                "dsdfasD1",
                List.of("사진2", "사진3"),
                null,
                false);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel123");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(1);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1");

        List<ProfileImageRegistration> list = profileImagesRegistrationRepository.findAll();
        assertThat(list).hasSize(1);
        assertThat(list).extracting("fileName").containsExactly("사진1");
    }


    @DisplayName("프로필 사진을 추가할 수 있다(1)")
    @Test
    void test12() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                Collections.emptyList(),
                List.of(new SavedImage("사진3", false)),
                null,
                false);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(3);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1", "사진2", "사진3");
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(true, false, false);

    }

    @DisplayName("프로필 사진을 추가할 수 있다(2)")
    @Test
    void test13() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                null,
                List.of(new SavedImage("사진3", false)),
                null,
                false);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(3);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1", "사진2", "사진3");
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(true, false, false);

    }

    @DisplayName("메인 사진만 변경할 수 있다(1)")
    @Test
    void test14() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");
        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                Collections.emptyList(),
                Collections.emptyList(),
                1,
                true);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(2);
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(false, true);

    }

    @DisplayName("메인 사진만 변경할 수 있다(2)")
    @Test
    void test15() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");
        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                Collections.emptyList(),
                null,
                1,
                true);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(2);
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(false, true);

    }


    @DisplayName("메인 사진만 변경할 수 있다(3)")
    @Test
    void test16() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");
        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                null,
                Collections.emptyList(),
                1,
                true);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(2);
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(false, true);

    }

    @DisplayName("프로필 사진 추가하고 메인 사진을 변경할 수 있다.")
    @Test
    void test17() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");
        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                List.of("사진1"),
                List.of(new SavedImage("사진3", true)),
                1,
                true);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(2);
        assertThat(profileImages).extracting("fileName").containsExactly("사진2", "사진3");
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(false, true);

    }

    @DisplayName("메인 사진을 취소하고 기존 사진에서 메인 사진을 설정한 뒤, 새로운 사진만 추가한다")
    @Test
    void test18() {

        userGenerator.generate("fnel1234", "123", "1", "연호2");
        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        profileImageServiceV1.updateImages(
                "fnel1234",
                null,
                List.of(new SavedImage("사진3", false)),
                1,
                true);

        Profile profile1 = profileRepository.findByUser_LoginId("fnel1234");
        List<ProfileImage> profileImages = profile1.getProfileImages();
        assertThat(profileImages).hasSize(3);
        assertThat(profileImages).extracting("fileName").containsExactly("사진1", "사진2", "사진3");
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(false, true, false);

    }

    @DisplayName("프로필을 변경할 때도 메인 사진이 있어야 한다")
    @Test
    void test19() {

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel123",
                List.of(new ProfileImageRequest(null, false)),
                "dddafda",
                List.of("사진1"),
                null,
                true))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진 중에 메인 사진을 골라야 합니다");
    }

    @DisplayName("프로필을 변경할 때도 메인 사진이 있어야 한다")
    @Test
    void test20() {

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel123",
                Collections.emptyList(),
                "dddafdads",
                List.of("사진1"),
                null,
                true))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진 중에 메인 사진을 골라야 합니다");
    }

    @DisplayName("프로필 사진 변경 시 메인 사진이 있는데, 메인 사진을 또 설정할 수 없다")
    @Test
    void test21() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");


        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, true)),
                "dfasd",
                Collections.emptyList(),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("메인 사진은 하나만 골라야 합니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, true)),
                "dfas3d2",
                null,
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("메인 사진은 하나만 골라야 합니다");
    }

    @DisplayName("프로필 사진 변경 시 메인 사진이 있는데, 메인 사진을 또 설정할 수 없다")
    @Test
    void test22() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");


        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, true)),
                "dfas34d",
                Collections.emptyList(),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("메인 사진은 하나만 골라야 합니다");

    }

    @DisplayName("프로필 사진을 업데이트할 때 하나 이상의 사진을 메인 사진으로 등록할 수 없다")
    @Test
    void test23() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, true), new ProfileImageRequest(null, true)),
                "dfasDDSAd",
                List.of("사진1"),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("메인 사진은 하나만 골라야 합니다");
    }

    @DisplayName("변경할 내용이 없는 요청은 실패한다")
    @Test
    void test24() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");


        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                null,
                "DFDASD1",
                Collections.emptyList(),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("변경할 이미지 정보가 없습니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                null,
                "dfasd",
                null,
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("변경할 이미지 정보가 없습니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                Collections.emptyList(),
                "dfasdsa",
                Collections.emptyList(),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("변경할 이미지 정보가 없습니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                Collections.emptyList(),
                "dfacdeesd",
                null,
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("변경할 이미지 정보가 없습니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                null,
                "dfaadsf1sd",
                Collections.emptyList(),
                null,
                true))
                .isInstanceOf(CustomException.class).hasMessage("변경할 이미지 정보가 없습니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                null,
                "dfasd11fd",
                null,
                null,
                true))
                .isInstanceOf(CustomException.class).hasMessage("변경할 이미지 정보가 없습니다");
    }

    @DisplayName("프로필 업데이트 시 중복된 요청을 걸러낸다")
    @Test
    void test25() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, true), new ProfileImageRequest(null, true)),
                "dfasDDSAd",
                List.of("사진1"),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("메인 사진은 하나만 골라야 합니다");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, true), new ProfileImageRequest(null, true)),
                "dfasDDSAd",
                List.of("사진1"),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("중복된 요청입니다");
    }

    @DisplayName("프로필 업데이트 시 사진은 반드시 하나 이상 보유해야 한다")
    @Test
    void test26() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                Collections.emptyList(),
                "dfasDDSAd",
                List.of("사진1", "사진2"),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진을 필수로 등록해야 합니다");
    }

    @DisplayName("프로필 업데이트 시 사진은 세개까지만 등록할 수 있다")
    @Test
    void test27() {
        userGenerator.generate("fnel1234", "123", "1", "연호2");

        profileImageServiceV1.saveImages(
                List.of(new SavedImage("사진1", true),
                        new SavedImage("사진2", false)),
                "fnel1234");

        assertThatThrownBy(() -> profileFacadeV1.updateImages(
                "fnel1234",
                List.of(new ProfileImageRequest(null, false), new ProfileImageRequest(null, false)),
                "dfasDDSAd",
                Collections.emptyList(),
                null,
                false))
                .isInstanceOf(CustomException.class).hasMessage("프로필 사진은 최대 3개까지만 등록 가능합니다");
    }

    @DisplayName("프로필 업데이트 시 사진에 대한 모니터링 자료가 남는다")
    @Test
    void test28() {


        profileImageServiceV1.updateImages(
                "fnel123",
                List.of("사진1"),
                List.of(new SavedImage("사진4", true)),
                null,
                false);

        Profile profile = profileFacadeV1.getProfileByLoginIdNotDTO("fnel123");
        List<ProfileImage> profileImages = profile.getProfileImages();
        assertThat(profileImages).hasSize(3);
        assertThat(profileImages).extracting("fileName").containsExactly("사진2", "사진3", "사진4");
        assertThat(profileImages).extracting("isMainPhoto").containsExactly(false, false, true);
        List<ProfileImageRegistration> all = profileImagesRegistrationRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).extracting("fileName").containsExactly("사진2", "사진3", "사진4");
        assertThat(profileImages).extracting("profileImageRegistration.fileName").containsExactly("사진2", "사진3", "사진4");
    }
}
