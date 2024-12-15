package com.plaything.api.domain.chat.service;

import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatList;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.chat.ChatRepository;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.plaything.api.domain.user.constants.Gender.M;
import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@SpringBootTest
public class ChatServiceTest {

    @Autowired
    private ChatServiceV1 chatServiceV1;

    @Autowired
    private ChatFacadeV1 chatFacadeV1;

    @Autowired
    private ChatRateLimiter rateLimiter;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRepository chatRepository;

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
        ProfileImage image1 = ProfileImage.builder().profile(profile1).isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));

        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "알렉2", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        Profile profile2 = profileRepository.findByNickName("알렉2");

        ProfileImage image2 = ProfileImage.builder().profile(profile2).isMainPhoto(true).build();
        profile2.addProfileImages(List.of(image2));

    }


    @DisplayName("채팅 메시지 내역을 조회할 수 있다.")
    @Test
    void test1() throws InterruptedException {

        LocalDateTime now = LocalDateTime.now();


        sendMessage("알렉1", "알렉2", "안녕", 1, now);
        sendMessage("알렉1", "알렉2", "반가워", 2, now);
        Thread.sleep(300);
        sendMessage("알렉1", "알렉2", "뭐해?", 3, now);

        sendMessage("알렉2", "알렉1", "하이", 4, now);
        sendMessage("알렉2", "알렉1", "그냥 쉬는중ㅋㅋ", 5, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);

        assertThat(chatRooms.get(0).partnerProfile().nickName()).isEqualTo("알렉2");
        assertThat(chatRooms.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(0).partnerProfile().mainPhoto()).isEqualTo("abcd");
        assertThat(chatRooms.get(0).lastChat()).isEqualTo("그냥 쉬는중ㅋㅋ");
        assertThat(chatRooms.get(0).lastChatSender()).isEqualTo("알렉2");


        ChatList chatList = chatFacadeV1.getChatList("dusgh1234", chatRooms.get(0).chatRoomId(), null);
        assertThat(chatList.message()).extracting("message")
                .containsExactly("그냥 쉬는중ㅋㅋ", "하이", "뭐해?", "반가워", "안녕");

        assertThat(chatList.message()).extracting("sequence")
                .containsExactly(5, 4, 3, 2, 1);

        assertThat(chatList.message()).extracting("senderNickname")
                .containsExactly("알렉2", "알렉2", "알렉1", "알렉1", "알렉1");
    }

    @DisplayName("채팅 메시지를 페이지네이션 할 수 있다")
    @Test
    void test2() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 20; i++) {
            sendMessage("알렉1", "알렉2", "시작" + i, i, now);
            Thread.sleep(300);
        }


        sendMessage("알렉1", "알렉2", "안녕", 1, now);
        sendMessage("알렉1", "알렉2", "반가워", 2, now);
        Thread.sleep(300);
        sendMessage("알렉1", "알렉2", "뭐해?", 3, now);

        sendMessage("알렉2", "알렉1", "하이", 4, now);
        sendMessage("알렉2", "알렉1", "그냥 쉬는중ㅋㅋ", 5, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);

        assertThat(chatRooms.get(0).partnerProfile().nickName()).isEqualTo("알렉2");
        assertThat(chatRooms.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(0).partnerProfile().mainPhoto()).isEqualTo("abcd");
        assertThat(chatRooms.get(0).lastChat()).isEqualTo("그냥 쉬는중ㅋㅋ");
        assertThat(chatRooms.get(0).lastChatSender()).isEqualTo("알렉2");


        ChatList chatList = chatFacadeV1.getChatList("dusgh1234", chatRooms.get(0).chatRoomId(), null);
        assertThat(chatList.message()).hasSize(10);
        assertThat(chatList.message()).extracting("message")
                .containsExactly("그냥 쉬는중ㅋㅋ", "하이", "뭐해?", "반가워", "안녕", "시작20", "시작19", "시작18", "시작17", "시작16");

        assertThat(chatList.message()).extracting("sequence")
                .containsExactly(25, 24, 23, 22, 21, 20, 19, 18, 17, 16);

        assertThat(chatList.message()).extracting("senderNickname")
                .containsExactly("알렉2", "알렉2", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1");


        ChatList chatList2 = chatFacadeV1.getChatList("dusgh1234", chatRooms.get(0).chatRoomId(), chatList.message().get(9).id());

        assertThat(chatList2.message()).hasSize(10);
        assertThat(chatList2.message()).extracting("message")
                .containsExactly("시작15", "시작14", "시작13", "시작12", "시작11", "시작10", "시작9", "시작8", "시작7", "시작6");

        assertThat(chatList2.message()).extracting("sequence")
                .containsExactly(15, 14, 13, 12, 11, 10, 9, 8, 7, 6);

        assertThat(chatList2.message()).extracting("senderNickname")
                .containsExactly("알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1");


        ChatList chatList3 = chatFacadeV1.getChatList("dusgh1234", chatRooms.get(0).chatRoomId(), chatList2.message().get(9).id());

        assertThat(chatList3.message()).hasSize(5);
        assertThat(chatList3.message()).extracting("message")
                .containsExactly("시작5", "시작4", "시작3", "시작2", "시작1");

        assertThat(chatList3.message()).extracting("sequence")
                .containsExactly(5, 4, 3, 2, 1);

        assertThat(chatList3.message()).extracting("senderNickname")
                .containsExactly("알렉1", "알렉1", "알렉1", "알렉1", "알렉1");

    }

    @DisplayName("채팅방 내용은 최대 일주일전까지만 볼 수 있다.")
    @Test
    void test3(
    ) {
        LocalDateTime date1 = LocalDateTime.now().minusDays(8);

        for (int i = 1; i <= 25; i++) {
            sendMessage("알렉1", "알렉2", "하이" + i, i, date1);
        }

        LocalDateTime date2 = LocalDateTime.now().minusDays(7);

        for (int i = 26; i <= 30; i++) {
            sendMessage("알렉1", "알렉2", "하이" + i, i, date2);

        }

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);

        ChatList chatList = chatFacadeV1.getChatList("dusgh1234", chatRooms.get(0).chatRoomId(), null);
        assertThat(chatList.message()).hasSize(5);
        assertThat(chatList.message()).extracting("message")
                .containsExactly("하이30", "하이29", "하이28", "하이27", "하이26");

        assertThat(chatList.message()).extracting("sequence")
                .containsExactly(30, 29, 28, 27, 26);

        assertThat(chatList.message()).extracting("senderNickname")
                .containsExactly("알렉1", "알렉1", "알렉1", "알렉1", "알렉1");

        assertThat(chatList.message()).extracting("createdAt")
                .containsExactly(date2, date2, date2, date2, date2);
    }

    @DisplayName("채팅 메시지를 보낼 때 누락된 메시지가 있으면 이를 받게 된다")
    @Test
    void test4() {
        LocalDateTime date1 = LocalDateTime.now();

        for (int i = 1; i <= 3; i++) {
            ChatWithMissingChat chatWithMissingChat = sendMessage("알렉1", "알렉2", "하이" + i, i, date1);

            assertThat(chatWithMissingChat.missingChat()).isEmpty();
            assertThat(chatWithMissingChat.chat().chat()).isEqualTo("하이" + i);
            assertThat(chatWithMissingChat.chat().receiverNickname()).isEqualTo("알렉2");
            assertThat(chatWithMissingChat.chat().senderNickname()).isEqualTo("알렉1");
            assertThat(chatWithMissingChat.chat().createdAt()).isEqualTo(date1);
            assertThat(chatWithMissingChat.chat().sequence()).isEqualTo(i);
        }


        ChatWithMissingChat chatWithMissingChat = sendMessage("알렉2", "알렉1", "하이~", 1, date1);

        assertThat(chatWithMissingChat.missingChat()).extracting("chat").containsExactly("하이2", "하이3");
        assertThat(chatWithMissingChat.missingChat()).extracting("senderNickname").containsExactly("알렉1", "알렉1");
        assertThat(chatWithMissingChat.missingChat()).extracting("receiverNickname").containsExactly("알렉2", "알렉2");
        assertThat(chatWithMissingChat.missingChat()).extracting("createdAt").containsExactly(date1, date1);
        assertThat(chatWithMissingChat.missingChat()).extracting("sequence").containsExactly(2, 3);

        assertThat(chatWithMissingChat.chat().chat()).isEqualTo("하이~");
        assertThat(chatWithMissingChat.chat().receiverNickname()).isEqualTo("알렉1");
        assertThat(chatWithMissingChat.chat().senderNickname()).isEqualTo("알렉2");
        assertThat(chatWithMissingChat.chat().createdAt()).isEqualTo(date1);
        assertThat(chatWithMissingChat.chat().sequence()).isEqualTo(4);

    }


    @DisplayName("채팅 메시지를 보낼 때 누락된 메시지가 있으면 이를 받게 된다")
    @Test
    void test5() {
        LocalDateTime date1 = LocalDateTime.now();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    sendMessage("알렉1", "알렉2", "하이" + index, index, date1);
                } finally {
                    countDownLatch.countDown(); // 작업 완료 시 카운트 감소
                }
            });
        }

        try {
            countDownLatch.await(); // 모든 작업이 완료될 때까지 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executorService.shutdown();
        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);

        ChatList chatList = chatFacadeV1.getChatList("dusgh1234", chatRooms.get(0).chatRoomId(), null);
        assertThat(chatList.message()).hasSize(5);
        assertThat(chatList.message()).extracting("message")
                .containsExactly("하이10", "하이9", "하이8", "하이7", "하이6", "하이5", "하이4", "하이3", "하이2", "하이1");

        assertThat(chatList.message()).extracting("sequence")
                .containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);

        assertThat(chatList.message()).extracting("senderNickname")
                .containsExactly("알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1", "알렉1");
    }


    private ChatWithMissingChat sendMessage(String sender, String reciver, String message, int sequence, LocalDateTime now) {
        ChatRequest msg = new ChatRequest(sequence, sender, reciver, message);
        return chatServiceV1.saveChatMessage(msg, now);
    }

}
