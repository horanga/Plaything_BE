package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringFacadeV1;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.index.service.IndexServiceV1;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.matching.service.MatchingServiceV1;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.UserServiceV1;
import com.plaything.api.domain.user.util.ImageUrlGenerator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.plaything.api.domain.user.constants.Gender.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@Transactional
@SpringBootTest
class ChatRoomServiceV1Test {

    @Autowired
    private ChatServiceV1 chatServiceV1;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ChatFacadeV1 chatFacadeV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ProfileMonitoringFacadeV1 profileMonitoringFacadeV1;

    @Autowired
    private UserServiceV1 userServiceV1;

    @Autowired
    private ChatRateLimiter rateLimiter;

    @Autowired
    private IndexServiceV1 indexServiceV1;

    @Autowired
    private MatchingServiceV1 matchingServiceV1;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @SpyBean
    RedisTemplate<String, String> mockRedis;

    @Autowired
    private ImageUrlGenerator imageUrlGenerator;

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @AfterEach
    void cleanUp() {
        rateLimiter.cleanupOldData();

    }

    @BeforeEach
    void setUp() {

        Set<String> keys = mockRedis.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            mockRedis.delete(keys);
        }
        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration("알렉1", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");

        Profile profile1 = profileRepository.findByNickName("알렉1");
        ProfileImage image1 = ProfileImage.builder().profile(profile1).fileName("abc").isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));

        ProfileRegistration profileRegistration2 = new ProfileRegistration("알렉2", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        Profile profile2 = profileRepository.findByNickName("알렉2");

        ProfileImage image2 = ProfileImage.builder().profile(profile2).fileName("abcd").isMainPhoto(true).build();
        profile2.addProfileImages(List.of(image2));


        LoginRequest dusgh1234 = new LoginRequest("dusgh1234", "1234");
        authServiceV1.login(dusgh1234, LocalDate.now(), "24324d");

        LoginRequest dusgh12345 = new LoginRequest("dusgh12345", "1234");
        authServiceV1.login(dusgh12345, LocalDate.now(), "24324dddd");

        User user1 = userServiceV1.findByLoginId("dusgh1234");
        User user2 = userServiceV1.findByLoginId("dusgh12345");
        matchingServiceV1.creatMatching(user1, user2, "3242df");
        matchingServiceV1.acceptMatching(user2, user1, "324dd2df");


    }

    @DisplayName("채팅방 목록에서 채팅방 정보를 조회할 수 있다.")
    @ParameterizedTest
    @MethodSource("chatProvider")
    void test1(String sender, String receiver, String senderLoginId, String partnerNickname, String partnerRole, String mainPhotoUrl) throws InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            ChatRequest msg = new ChatRequest(i, sender, receiver, "안녕" + i);
            chatServiceV1.saveChatMessage(msg, now);
            Thread.sleep(1000);
        }

        ChatRequest msg = new ChatRequest(11, sender, receiver, "안녕하십니까!");
        chatServiceV1.saveChatMessage(msg, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, null);

        assertThat(chatRooms.get(0).partnerProfile().nickName()).isEqualTo(partnerNickname);
        assertThat(chatRooms.get(0).partnerProfile().primaryRole()).isEqualTo(partnerRole);
        assertThat(chatRooms.get(0).partnerProfile().mainPhoto()).isEqualTo("https://" + cloudfrontDomain + mainPhotoUrl);

        assertThat(chatRooms.get(0).lastChat()).isEqualTo("안녕하십니까!");

    }

    public static Stream<Arguments> chatProvider() {

        return Stream.of(Arguments.of("알렉1", "알렉2", "dusgh1234", "알렉2", "MT", "/abcd"), Arguments.of("알렉2", "알렉1", "dusgh12345", "알렉1", "MT", "/abc"));
    }

    @DisplayName("채팅방은 채팅 메시지가 오면 마지막 채팅 관련 정보가 변경된다")
    @ParameterizedTest
    @MethodSource("chatProvider2")
    void test2(String sender, String receiver, String senderLoginId) throws InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 4; i++) {
            ChatRequest msg = new ChatRequest(i, sender, receiver, "안녕" + i);
            chatServiceV1.saveChatMessage(msg, now);
            Thread.sleep(1000);
        }

        ChatRequest msg = new ChatRequest(4, receiver, sender, "안녕하십니까!");
        chatServiceV1.saveChatMessage(msg, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, null);
        assertThat(chatRooms.get(0).lastChat()).isEqualTo("안녕하십니까!");
        assertThat(chatRooms.get(0).lastChatSender()).isEqualTo(receiver);
        ChatRoom room = chatRoomRepository.findChatRoomByUsers("알렉1", "알렉2").get();
        assertThat(room.getLastSequence()).isEqualTo(5);
    }

    public static Stream<Arguments> chatProvider2() {

        return Stream.of(Arguments.of("알렉1", "알렉2", "dusgh1234"), Arguments.of("알렉2", "알렉1", "dusgh12345"));
    }

    @DisplayName("채팅방 목록을 10개씩 조회할 수 있다.")
    @Test
    void test3() throws InterruptedException {

        for (int i = 1; i <= 30; i++) {

            LoginRequest a = new LoginRequest("dusgh1234", "1234");
            authServiceV1.login(a, LocalDate.now(), "24324d" + i);

            createUser("dusgh" + i, "연" + i, "abc" + i);

            LoginRequest b = new LoginRequest("dusgh" + i, "1234");
            authServiceV1.login(b, LocalDate.now(), "24324dddd" + i);

            User user1 = userServiceV1.findByLoginId("dusgh1234");
            User user2 = userServiceV1.findByLoginId("dusgh" + i);

            matchingServiceV1.creatMatching(user1, user2, "3242df" + i);
            matchingServiceV1.acceptMatching(user2, user1, "324dd2df" + i);

            sendMessage("알렉1", "연" + i, String.valueOf(i));
            Thread.sleep(300);
        }

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms).extracting(
                        "partnerProfile.mainPhoto",
                        "partnerProfile.primaryRole",
                        "partnerProfile.nickName")
                .containsExactly(
                        tuple(imageUrlGenerator.getImageUrl("abc30"), "MT", "연30"),
                        tuple(imageUrlGenerator.getImageUrl("abc29"), "MT", "연29"),
                        tuple(imageUrlGenerator.getImageUrl("abc28"), "MT", "연28"),
                        tuple(imageUrlGenerator.getImageUrl("abc27"), "MT", "연27"),
                        tuple(imageUrlGenerator.getImageUrl("abc26"), "MT", "연26"),
                        tuple(imageUrlGenerator.getImageUrl("abc25"), "MT", "연25"),
                        tuple(imageUrlGenerator.getImageUrl("abc24"), "MT", "연24"),
                        tuple(imageUrlGenerator.getImageUrl("abc23"), "MT", "연23"),
                        tuple(imageUrlGenerator.getImageUrl("abc22"), "MT", "연22"),
                        tuple(imageUrlGenerator.getImageUrl("abc21"), "MT", "연21"));

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", chatRooms.get(9).chatRoomId());
        assertThat(chatRooms2).extracting(
                        "partnerProfile.mainPhoto",
                        "partnerProfile.primaryRole",
                        "partnerProfile.nickName")
                .containsExactly(
                        tuple(imageUrlGenerator.getImageUrl("abc20"), "MT", "연20"),
                        tuple(imageUrlGenerator.getImageUrl("abc19"), "MT", "연19"),
                        tuple(imageUrlGenerator.getImageUrl("abc18"), "MT", "연18"),
                        tuple(imageUrlGenerator.getImageUrl("abc17"), "MT", "연17"),
                        tuple(imageUrlGenerator.getImageUrl("abc16"), "MT", "연16"),
                        tuple(imageUrlGenerator.getImageUrl("abc15"), "MT", "연15"),
                        tuple(imageUrlGenerator.getImageUrl("abc14"), "MT", "연14"),
                        tuple(imageUrlGenerator.getImageUrl("abc13"), "MT", "연13"),
                        tuple(imageUrlGenerator.getImageUrl("abc12"), "MT", "연12"),
                        tuple(imageUrlGenerator.getImageUrl("abc11"), "MT", "연11"));

        List<ChatRoomResponse> chatRooms3 = chatFacadeV1.getChatRooms("dusgh1234", chatRooms2.get(9).chatRoomId());
        assertThat(chatRooms3).extracting(
                        "partnerProfile.mainPhoto",
                        "partnerProfile.primaryRole",
                        "partnerProfile.nickName")
                .containsExactly(
                        tuple(imageUrlGenerator.getImageUrl("abc10"), "MT", "연10"),
                        tuple(imageUrlGenerator.getImageUrl("abc9"), "MT", "연9"),
                        tuple(imageUrlGenerator.getImageUrl("abc8"), "MT", "연8"),
                        tuple(imageUrlGenerator.getImageUrl("abc7"), "MT", "연7"),
                        tuple(imageUrlGenerator.getImageUrl("abc6"), "MT", "연6"),
                        tuple(imageUrlGenerator.getImageUrl("abc5"), "MT", "연5"),
                        tuple(imageUrlGenerator.getImageUrl("abc4"), "MT", "연4"),
                        tuple(imageUrlGenerator.getImageUrl("abc3"), "MT", "연3"),
                        tuple(imageUrlGenerator.getImageUrl("abc2"), "MT", "연2"),
                        tuple(imageUrlGenerator.getImageUrl("abc1"), "MT", "연1"));


    }


    @DisplayName("상대방이 채팅방을 나가면 채팅방에 정보에 표시된다")
    @Test
    void test4() throws InterruptedException {
        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");

        LoginRequest a = new LoginRequest("dusgh12345", "1234");
        authServiceV1.login(a, LocalDate.now(), "24324d");
        LoginRequest b = new LoginRequest("dusgh12", "1234");
        authServiceV1.login(b, LocalDate.now(), "24324dd");
        LoginRequest c = new LoginRequest("dusgh123", "1234");
        authServiceV1.login(c, LocalDate.now(), "24324ddd");

        User user1 = userServiceV1.findByLoginId("dusgh1234");
        User user2 = userServiceV1.findByLoginId("dusgh12");
        User user3 = userServiceV1.findByLoginId("dusgh123");

        matchingServiceV1.creatMatching(user1, user2, "3242df");
        matchingServiceV1.acceptMatching(user2, user1, "324dd2df");

        matchingServiceV1.creatMatching(user1, user3, "3242df");
        matchingServiceV1.acceptMatching(user3, user1, "324dd2df");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        Thread.sleep(1000);
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms.size()).isEqualTo(3);

        assertThat(chatRooms.get(2).lastChat()).isEqualTo("hi");
        assertThat(chatRooms.get(2).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(2).partnerProfile().nickName()).isEqualTo("알렉2");

        assertThat(chatRooms.get(1).lastChat()).isEqualTo("hi~");
        assertThat(chatRooms.get(1).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(1).partnerProfile().nickName()).isEqualTo("연1");

        assertThat(chatRooms.get(0).lastChat()).isEqualTo("hello");
        assertThat(chatRooms.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(0).partnerProfile().nickName()).isEqualTo("연2");

        chatFacadeV1.leaveChatRoom(chatRooms.get(0).chatRoomId(), "dusgh1234");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12345", null);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRooms.get(0).chatRoomId()).get();

        assertThat(chatRoom.getExitedUserNickname()).isEqualTo("알렉1");
        assertThat(chatRooms2).hasSize(1);

    }

    @DisplayName("이미 종료된 채팅방은 조회하지 못한다")
    @Test
    void test5() throws InterruptedException {
        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        Thread.sleep(1000);
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh1234");
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");

        List<ChatRoomResponse> chatRooms1 = chatFacadeV1.getChatRooms("dusgh1234", null);
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);

        assertThat(chatRooms1.size()).isEqualTo(2);
        assertThat(chatRooms2).isEmpty();

        assertThat(chatRooms1.get(1).lastChat()).isEqualTo("hello");
        assertThat(chatRooms1.get(1).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms1.get(1).partnerProfile().nickName()).isEqualTo("연2");

        assertThat(chatRooms1.get(0).lastChat()).isEqualTo("hi");
        assertThat(chatRooms1.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms1.get(0).partnerProfile().nickName()).isEqualTo("알렉2");
    }

    @DisplayName("종료된 채팅방은 채팅 내역을 조회할 수 없다")
    @Test
    void test6() throws InterruptedException {
        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        Thread.sleep(1000);
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh1234");
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh1234", chatRooms.get(2).chatRoomId(), null)).isInstanceOf(CustomException.class).hasMessage("이미 종료된 채팅방입니다");

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh12", chatRooms.get(2).chatRoomId(), null)).isInstanceOf(CustomException.class).hasMessage("이미 종료된 채팅방입니다");
    }

    @DisplayName("상대방이 나간 채팅 내역을 조회할 수 없다")
    @Test
    void test7() {

        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");


        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms2.size()).isEqualTo(3);

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh1234", chatRooms.get(2).chatRoomId(), null)).isInstanceOf(CustomException.class).hasMessage("상대방이 채팅방을 떠났습니다");
    }

    @DisplayName("자신이 나간 채팅 내역을 조회할 수 없다")
    @Test
    void test8() throws InterruptedException {

        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        Thread.sleep(1000);
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);
        assertThat(chatRooms.size()).isEqualTo(3);
        assertThat(chatRooms2.size()).isEqualTo(0);
    }

    @DisplayName("밴 당한 유저와의 채팅방은 조회되지 않는다")
    @Test
    void test9() {

        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        profileMonitoringFacadeV1.rejectProfile(records.get(1).recordId(), "부적절한 사진");

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms.size()).isEqualTo(3);
        assertThat(chatRooms2.size()).isEqualTo(2);

        assertThat(chatRooms2.get(0).lastChat()).isEqualTo("hello");
        assertThat(chatRooms2.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms2.get(0).partnerProfile().nickName()).isEqualTo("연2");

        assertThat(chatRooms2.get(1).lastChat()).isEqualTo("hi~");
        assertThat(chatRooms2.get(1).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms2.get(1).partnerProfile().nickName()).isEqualTo("연1");

    }

    @DisplayName("탈퇴한 유저와의 채팅방은 조회되지 않는다")
    @Test
    void test10() {

        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);

        userServiceV1.delete("dusgh12345");

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms.size()).isEqualTo(3);
        assertThat(chatRooms2.size()).isEqualTo(2);

        assertThat(chatRooms2.get(0).lastChat()).isEqualTo("hello");
        assertThat(chatRooms2.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms2.get(0).partnerProfile().nickName()).isEqualTo("연2");


        assertThat(chatRooms2.get(1).lastChat()).isEqualTo("hi~");
        assertThat(chatRooms2.get(1).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms2.get(1).partnerProfile().nickName()).isEqualTo("연1");

    }

    @DisplayName("메시지를 보내면 새로운 메시지 표시가 뜬다")
    @Test
    void test11() {

        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");

        IndexResponse index1 = indexServiceV1.refreshIndex("dusgh12");
        IndexResponse index2 = indexServiceV1.refreshIndex("dusgh1234");

        assertThat(index1.hasNewChat()).isTrue();
        assertThat(index2.hasNewChat()).isFalse();

    }

    @DisplayName("메시지를 조회하면 새로운 메시지 표시가 사라진다")
    @Test
    void test12() {

        createUser("dusgh12", "연1", "abc");
        createUser("dusgh123", "연2", "abc1");

        sendMessage("알렉1", "연1", "hi~");
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByUsers("알렉1", "연1").get();
        chatFacadeV1.getChatList("dusgh1234", chatRoom.getId(), null);

        IndexResponse index1 = indexServiceV1.refreshIndex("dusgh12");
        IndexResponse index2 = indexServiceV1.refreshIndex("dusgh1234");

        assertThat(index1.hasNewChat()).isTrue();
        assertThat(index2.hasNewChat()).isFalse();

        chatFacadeV1.getChatList("dusgh12", chatRoom.getId(), null);

        IndexResponse index3 = indexServiceV1.refreshIndex("dusgh12");
        IndexResponse index4 = indexServiceV1.refreshIndex("dusgh1234");

        assertThat(index3.hasNewChat()).isFalse();
        assertThat(index4.hasNewChat()).isFalse();
    }


    private void createUser(String loginId, String nickName, String filename) {
        CreateUserRequest request = new CreateUserRequest(loginId, "1234", "1");
        authServiceV1.creatUser(request);
        setProfile(loginId, nickName, filename);
    }

    private ChatWithMissingChat sendMessage(String sender, String receiver, String message) {
        ChatRequest msg = new ChatRequest(0, sender, receiver, message);
        LocalDateTime now = LocalDateTime.now();
        return chatServiceV1.saveChatMessage(msg, now);
    }

    private void setProfile(String id, String name, String fileName) {
        LocalDate now = LocalDate.now();

        ProfileRegistration profileRegistration = new ProfileRegistration(name, "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        profileFacadeV1.registerProfile(profileRegistration, id);
        Profile profile1 = profileRepository.findByNickName(name);
        ProfileImage image1 = ProfileImage.builder().profile(profile1).fileName(fileName).isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));
    }
}