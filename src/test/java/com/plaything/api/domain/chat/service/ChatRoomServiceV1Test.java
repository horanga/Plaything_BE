package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringFacadeV1;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.index.service.IndexServiceV1;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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


    @AfterEach
    void cleanUp() {
        rateLimiter.cleanupOldData();
    }

    @BeforeEach
    void setUp() {
        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "알렉1", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");

        Profile profile1 = profileRepository.findByNickName("알렉1");
        ProfileImage image1 = ProfileImage.builder().profile(profile1).url("abc").isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));

        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "알렉2", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        Profile profile2 = profileRepository.findByNickName("알렉2");

        ProfileImage image2 = ProfileImage.builder().profile(profile2).url("abcd").isMainPhoto(true).build();
        profile2.addProfileImages(List.of(image2));

    }

    @DisplayName("채팅방 목록에서 채팅방 정보를 조회할 수 있다.")
    @ParameterizedTest
    @MethodSource("chatProvider")
    void test1(
            String sender,
            String receiver,
            String senderLoginId,
            String partnerNickname,
            String partnerRole,
            String mainPhotoUrl
    ) throws InterruptedException {

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
        assertThat(chatRooms.get(0).partnerProfile().mainPhoto()).isEqualTo(mainPhotoUrl);

        assertThat(chatRooms.get(0).lastChat()).isEqualTo("안녕하십니까!");

    }

    public static Stream<Arguments> chatProvider() {

        return Stream.of(
                Arguments.of("알렉1",
                        "알렉2",
                        "dusgh1234",
                        "알렉2",
                        "MT",
                        "abcd"
                ),
                Arguments.of("알렉2",
                        "알렉1",
                        "dusgh12345",
                        "알렉1",
                        "MT",
                        "abc"
                )
        );
    }

    @DisplayName("채팅방은 채팅 메시지가 오면 마지막 채팅 관련 정보가 변경된다")
    @ParameterizedTest
    @MethodSource("chatProvider2")
    void test2(
            String sender,
            String receiver,
            String senderLoginId
    ) throws InterruptedException {

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

        return Stream.of(
                Arguments.of("알렉1",
                        "알렉2",
                        "dusgh1234"
                ),
                Arguments.of("알렉2",
                        "알렉1",
                        "dusgh12345"
                )
        );
    }

    @DisplayName("채팅방 목록을 10개씩 조회할 수 있다.")
    @Test
    void test3() throws InterruptedException {

        for (int i = 1; i <= 30; i++) {
            createUser("dusgh" + i, "연" + i);
            sendMessage("알렉1", "연" + i, String.valueOf(i));
            Thread.sleep(300);
        }

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms).extracting("partnerProfile.mainPhoto", "partnerProfile.primaryRole", "partnerProfile.nickName")
                .containsExactly(
                        tuple("abc", "MT", "연30"),
                        tuple("abc", "MT", "연29"),
                        tuple("abc", "MT", "연28"),
                        tuple("abc", "MT", "연27"),
                        tuple("abc", "MT", "연26"),
                        tuple("abc", "MT", "연25"),
                        tuple("abc", "MT", "연24"),
                        tuple("abc", "MT", "연23"),
                        tuple("abc", "MT", "연22"),
                        tuple("abc", "MT", "연21")
                );

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", chatRooms.get(9).chatRoomId());
        assertThat(chatRooms2).extracting("partnerProfile.mainPhoto", "partnerProfile.primaryRole", "partnerProfile.nickName")
                .containsExactly(
                        tuple("abc", "MT", "연20"),
                        tuple("abc", "MT", "연19"),
                        tuple("abc", "MT", "연18"),
                        tuple("abc", "MT", "연17"),
                        tuple("abc", "MT", "연16"),
                        tuple("abc", "MT", "연15"),
                        tuple("abc", "MT", "연14"),
                        tuple("abc", "MT", "연13"),
                        tuple("abc", "MT", "연12"),
                        tuple("abc", "MT", "연11")
                );

        List<ChatRoomResponse> chatRooms3 = chatFacadeV1.getChatRooms("dusgh1234", chatRooms2.get(9).chatRoomId());
        assertThat(chatRooms3).extracting("partnerProfile.mainPhoto", "partnerProfile.primaryRole", "partnerProfile.nickName")
                .containsExactly(
                        tuple("abc", "MT", "연10"),
                        tuple("abc", "MT", "연9"),
                        tuple("abc", "MT", "연8"),
                        tuple("abc", "MT", "연7"),
                        tuple("abc", "MT", "연6"),
                        tuple("abc", "MT", "연5"),
                        tuple("abc", "MT", "연4"),
                        tuple("abc", "MT", "연3"),
                        tuple("abc", "MT", "연2"),
                        tuple("abc", "MT", "연1")
                );


    }


    @DisplayName("상대방이 채팅방을 나가면 채팅방에 정보에 표시된다")
    @Test
    void test4() throws InterruptedException {
        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        Thread.sleep(1000);
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms.size()).isEqualTo(3);

        assertThat(chatRooms.get(2).lastChat()).isEqualTo("hi~");
        assertThat(chatRooms.get(2).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(2).partnerProfile().nickName()).isEqualTo("연1");

        assertThat(chatRooms.get(1).lastChat()).isEqualTo("hello");
        assertThat(chatRooms.get(1).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(1).partnerProfile().nickName()).isEqualTo("연2");

        assertThat(chatRooms.get(0).lastChat()).isEqualTo("hi");
        assertThat(chatRooms.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(0).partnerProfile().nickName()).isEqualTo("알렉2");

        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh1234");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRooms.get(2).chatRoomId()).get();

        assertThat(chatRoom.getExitedUserNickname()).isEqualTo("알렉1");
        assertThat(chatRooms2.size()).isEqualTo(1);

        assertThat(chatRooms2.get(0).lastChat()).isEqualTo("hi~");
        assertThat(chatRooms2.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms2.get(0).partnerProfile().nickName()).isEqualTo("알렉1");
    }

    @DisplayName("이미 종료된 채팅방은 조회하지 못한다")
    @Test
    void test5() throws InterruptedException {
        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

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
        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        Thread.sleep(1000);
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh1234");
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh1234", chatRooms.get(2).chatRoomId(), null))
                .isInstanceOf(CustomException.class).hasMessage("이미 종료된 채팅방입니다");

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh12", chatRooms.get(2).chatRoomId(), null))
                .isInstanceOf(CustomException.class).hasMessage("이미 종료된 채팅방입니다");
    }

    @DisplayName("상대방이 나간 채팅 내역을 조회할 수 없다")
    @Test
    void test7() {

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");


        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms2.size()).isEqualTo(3);

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh1234", chatRooms.get(2).chatRoomId(), null))
                .isInstanceOf(CustomException.class).hasMessage("상대방이 채팅방을 떠났습니다");
    }

    @DisplayName("자신이 나간 채팅 내역을 조회할 수 없다")
    @Test
    void test8() throws InterruptedException {

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

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

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

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

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

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

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

        sendMessage("알렉1", "연1", "hi~");

        IndexResponse index1 = indexServiceV1.refreshIndex("dusgh12");
        IndexResponse index2 = indexServiceV1.refreshIndex("dusgh1234");

        assertThat(index1.hasNewChat()).isTrue();
        assertThat(index2.hasNewChat()).isFalse();

    }

    @DisplayName("메시지를 조회하면 새로운 메시지 표시가 사라진다")
    @Test
    void test12() {

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

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


    private void createUser(String loginId, String nickName) {
        CreateUserRequest request = new CreateUserRequest(loginId, "1234", "1");
        authServiceV1.creatUser(request);
        setProfile(loginId, nickName);
    }

    private ChatWithMissingChat sendMessage(String sender, String receiver, String message) {
        ChatRequest msg = new ChatRequest(0, sender, receiver, message);
        LocalDateTime now = LocalDateTime.now();
        return chatServiceV1.saveChatMessage(msg, now);
    }

    private void setProfile(String id, String name) {
        LocalDate now = LocalDate.now();

        ProfileRegistration profileRegistration = new ProfileRegistration(
                name, "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        profileFacadeV1.registerProfile(profileRegistration, id);
        Profile profile1 = profileRepository.findByNickName(name);
        ProfileImage image1 = ProfileImage.builder().profile(profile1).url("abc").isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));
    }
}