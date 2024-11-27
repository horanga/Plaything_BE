package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.Message;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.repository.entity.chat.Chat;
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
import jakarta.transaction.Transactional;
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
    ) {

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            Message msg = new Message(sender, receiver, "안녕" + i);
            chatServiceV1.saveChatMessage(msg, now);
        }

        Message msg = new Message(sender, receiver, "안녕하십니까!");
        Chat chat = chatServiceV1.saveChatMessage(msg, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, null);

        assertThat(chatRooms.get(0).chatProfile().nickName()).isEqualTo(partnerNickname);
        assertThat(chatRooms.get(0).chatProfile().primaryRole()).isEqualTo(partnerRole);
        assertThat(chatRooms.get(0).chatProfile().mainPhoto()).isEqualTo(mainPhotoUrl);

        assertThat(chatRooms.get(0).lastChatMessage()).isEqualTo("안녕하십니까!");
        assertThat(chatRooms.get(0).lastMessageAt()).isEqualTo(chat.getCreatedAt());

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
    ) {

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            Message msg = new Message(sender, receiver, "안녕" + i);
            chatServiceV1.saveChatMessage(msg, now);
        }

        Message msg = new Message(sender, receiver, "안녕하십니까!");
        Chat chat = chatServiceV1.saveChatMessage(msg, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, null);
        assertThat(chatRooms.get(0).lastChatMessage()).isEqualTo("안녕하십니까!");
        assertThat(chatRooms.get(0).lastMessageAt()).isEqualTo(chat.getCreatedAt());

        Message msg2 = new Message(sender, receiver, "반가워요 저는 알렉스입니다");
        Chat chat2 = chatServiceV1.saveChatMessage(msg2, now);

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms(senderLoginId, null);
        assertThat(chatRooms2.get(0).lastChatMessage()).isEqualTo("반가워요 저는 알렉스입니다");
        assertThat(chatRooms2.get(0).lastMessageAt()).isEqualTo(chat2.getCreatedAt());


        Message msg3 = new Message(sender, receiver, "오늘 기분은 어떠세요?");
        Chat chat3 = chatServiceV1.saveChatMessage(msg3, now);

        List<ChatRoomResponse> chatRooms3 = chatFacadeV1.getChatRooms(senderLoginId, null);
        assertThat(chatRooms3.get(0).lastChatMessage()).isEqualTo("오늘 기분은 어떠세요?");
        assertThat(chatRooms3.get(0).lastMessageAt()).isEqualTo(chat3.getCreatedAt());

    }

    public static Stream<Arguments> chatProvider2() {

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

    @DisplayName("채팅방 목록을 조회하면 10개의 최신 채팅방과 ")
    @Test
    void test4() {

        for (int i = 1; i <= 30; i++) {
            createUser("dusgh" + i, "연" + i);
            sendMessage("알렉1", "연" + i, String.valueOf(i));
        }


        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms).extracting("lastChatMessage").containsExactly(
                "30",
                "29",
                "28",
                "27",
                "26",
                "25",
                "24",
                "23",
                "22",
                "21");
        assertThat(chatRooms)
                .extracting(
                        "chatProfile.mainPhoto",
                        "chatProfile.primaryRole",
                        "chatProfile.nickName"
                )
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
        assertThat(chatRooms2).extracting("lastChatMessage").containsExactly(
                "20",
                "19",
                "18",
                "17",
                "16",
                "15",
                "14",
                "13",
                "12",
                "11");
        assertThat(chatRooms2)
                .extracting(
                        "chatProfile.mainPhoto",
                        "chatProfile.primaryRole",
                        "chatProfile.nickName"
                )
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
        assertThat(chatRooms3).extracting("lastChatMessage").containsExactly(
                "10",
                "9",
                "8",
                "7",
                "6",
                "5",
                "4",
                "3",
                "2",
                "1");
        assertThat(chatRooms3)
                .extracting(
                        "chatProfile.mainPhoto",
                        "chatProfile.primaryRole",
                        "chatProfile.nickName"
                )
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
    void test5() {
        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");


        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms.size()).isEqualTo(3);

        assertThat(chatRooms.get(2).lastChatMessage()).isEqualTo("hi~");
        assertThat(chatRooms.get(2).chatProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(2).chatProfile().nickName()).isEqualTo("연1");

        assertThat(chatRooms.get(1).lastChatMessage()).isEqualTo("hello");
        assertThat(chatRooms.get(1).chatProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(1).chatProfile().nickName()).isEqualTo("연2");

        assertThat(chatRooms.get(0).lastChatMessage()).isEqualTo("hi");
        assertThat(chatRooms.get(0).chatProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(0).chatProfile().nickName()).isEqualTo("알렉2");

        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh1234");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRooms.get(2).chatRoomId()).get();

        assertThat(chatRoom.getExitedUserNickname()).isEqualTo("알렉1");
        assertThat(chatRooms2.size()).isEqualTo(1);

        assertThat(chatRooms2.get(0).lastChatMessage()).isEqualTo("hi~");
        assertThat(chatRooms2.get(0).chatProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms2.get(0).chatProfile().nickName()).isEqualTo("알렉1");
    }

    @DisplayName("이미 종료된 채팅방은 조회하지 못한다")
    @Test
    void test6() {
        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");


        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh1234");
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");


        List<ChatRoomResponse> chatRooms1 = chatFacadeV1.getChatRooms("dusgh1234", null);
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);

        assertThat(chatRooms1.size()).isEqualTo(2);
        assertThat(chatRooms2).isEmpty();

        assertThat(chatRooms1.get(1).lastChatMessage()).isEqualTo("hello");
        assertThat(chatRooms1.get(1).chatProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms1.get(1).chatProfile().nickName()).isEqualTo("연2");

        assertThat(chatRooms1.get(0).lastChatMessage()).isEqualTo("hi");
        assertThat(chatRooms1.get(0).chatProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms1.get(0).chatProfile().nickName()).isEqualTo("알렉2");
    }

    @DisplayName("종료된 채팅방은 채팅 내역을 조회할 수 없다")
    @Test
    void test7() {
        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
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
    void test8() {

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
    void test9() {

        createUser("dusgh12", "연1");
        createUser("dusgh123", "연2");

        sendMessage("알렉1", "연1", "hi~");
        sendMessage("알렉1", "연2", "hello");
        sendMessage("알렉1", "알렉2", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(2).chatRoomId(), "dusgh12");

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);
        assertThat(chatRooms.size()).isEqualTo(3);
        assertThat(chatRooms2.size()).isEqualTo(0);
    }

    private void createUser(String loginId, String nickName) {
        CreateUserRequest request = new CreateUserRequest(loginId, "1234", "1");
        authServiceV1.creatUser(request);
        setProfile(loginId, nickName);
    }

    private Chat sendMessage(String sender, String receiver, String message) {
        Message msg = new Message(sender, receiver, message);
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