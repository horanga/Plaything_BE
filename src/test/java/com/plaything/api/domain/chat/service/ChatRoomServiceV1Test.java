package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringFacadeV1;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.index.service.IndexServiceV1;
import com.plaything.api.domain.key.constant.PointStatus;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.pay.PointKey;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.user.service.UserServiceV1;
import com.plaything.api.domain.user.util.ImageUrlGenerator;
import com.plaything.api.util.UserGenerator;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@Transactional
@SpringBootTest
class ChatRoomServiceV1Test {

    @Autowired
    private ChatServiceV1 chatServiceV1;

    @Autowired
    private ChatFacadeV1 chatFacadeV1;
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

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @SpyBean
    RedisTemplate<String, String> mockRedis;

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private ImageUrlGenerator imageUrlGenerator;

    @Autowired
    private PointKeyRepository pointKeyRepository;

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

        userGenerator.generate("dusgh1234", "1234", "1", "알렉1");
        userGenerator.generate("dusgh12345", "1234", "1", "알렉2");
        userGenerator.generate("dusgh12", "1234", "2", "연1");
        userGenerator.generate("dusgh123", "1234", "2", "연2");

        userGenerator.addImages("알렉1", "abc");
        userGenerator.addImages("알렉2", "abcd");
        userGenerator.addImages("연1", "dd");
        userGenerator.addImages("연2", "dd");

        userGenerator.createMatching("dusgh1234", "1234", "dusgh12345", "1234");

        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");

        userGenerator.createMatching("dusgh1234", "1234", "dusgh12", "1234");
        userGenerator.createMatching("dusgh1234", "1234", "dusgh123", "1234");


    }

    @DisplayName("채팅방 목록에서 채팅방 정보를 조회할 수 있다.")
    @ParameterizedTest
    @MethodSource("chatProvider")
    void test1(String sender,
               String receiver,
               String partnerNickname,
               String partnerRole,
               String mainPhotoUrl,
               int index) throws InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            ChatRequest msg = new ChatRequest(i, sender, receiver, "안녕" + i);
            chatServiceV1.saveChatMessage(msg, now);
            Thread.sleep(500);
        }

        ChatRequest msg = new ChatRequest(11, sender, receiver, "안녕하십니까!");
        chatServiceV1.saveChatMessage(msg, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(sender, null);

        assertThat(chatRooms.get(index).partnerProfile().nickName()).isEqualTo(partnerNickname);
        assertThat(chatRooms.get(index).partnerProfile().primaryRole()).isEqualTo(partnerRole);
        assertThat(chatRooms.get(index).partnerProfile().mainPhoto()).isEqualTo("https://" + cloudfrontDomain + mainPhotoUrl);

        assertThat(chatRooms.get(index).lastChat()).isEqualTo("안녕하십니까!");

    }

    public static Stream<Arguments> chatProvider() {

        return Stream.of(Arguments.of("dusgh1234", "dusgh12345", "알렉2", "MT", "/abcd", 2),
                Arguments.of("dusgh12345", "dusgh1234", "알렉1", "MT", "/abc", 0));
    }

    @DisplayName("채팅방은 채팅 메시지가 오면 마지막 채팅 관련 정보가 변경된다")
    @Test
    void test2() throws InterruptedException {

        String senderLoginId = "dusgh1234";
        String receiverLoginId = "dusgh12345";

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 4; i++) {
            ChatRequest msg = new ChatRequest(i, senderLoginId, receiverLoginId, "안녕" + i);
            chatServiceV1.saveChatMessage(msg, now);
            Thread.sleep(1000);
        }

        ChatRequest msg = new ChatRequest(4, receiverLoginId, senderLoginId, "안녕하십니까!");
        chatServiceV1.saveChatMessage(msg, now);

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, null);
        assertThat(chatRooms.get(2).lastChat()).isEqualTo("안녕하십니까!");
        assertThat(chatRooms.get(2).lastChatSender()).isEqualTo(receiverLoginId);
        ChatRoom room = chatRoomRepository.findChatRoomByUsers(senderLoginId, receiverLoginId).get();
        assertThat(room.getLastSequence()).isEqualTo(5);
    }

    @DisplayName("채팅방 목록을 10개씩 조회할 수 있다.")
    @Test
    void test3() throws InterruptedException {

        User user = userServiceV1.findByLoginId("dusgh1234");

        for (int i = 0; i < 50; i++) {
            pointKeyRepository.save(PointKey.builder().isValidKey(true).expirationDate(LocalDateTime.now().plusDays(10)).user(user).status(PointStatus.EARN).build());
        }

        for (int i = 30; i <= 60; i++) {

            userGenerator.generate("dusgh" + i, "1234", "2", "연" + i);
            userGenerator.addImages("연" + i, "abc" + i);
            userGenerator.createMatching("dusgh1234", "1234", "dusgh" + i, "1234");

            sendMessage("dusgh1234", "dusgh" + i, String.valueOf(i));
            Thread.sleep(300);
        }

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms).extracting(
                        "partnerProfile.mainPhoto",
                        "partnerProfile.primaryRole",
                        "partnerProfile.nickName")
                .containsExactly(
                        tuple(imageUrlGenerator.getImageUrl("abc60"), "MT", "연60"),
                        tuple(imageUrlGenerator.getImageUrl("abc59"), "MT", "연59"),
                        tuple(imageUrlGenerator.getImageUrl("abc58"), "MT", "연58"),
                        tuple(imageUrlGenerator.getImageUrl("abc57"), "MT", "연57"),
                        tuple(imageUrlGenerator.getImageUrl("abc56"), "MT", "연56"),
                        tuple(imageUrlGenerator.getImageUrl("abc55"), "MT", "연55"),
                        tuple(imageUrlGenerator.getImageUrl("abc54"), "MT", "연54"),
                        tuple(imageUrlGenerator.getImageUrl("abc53"), "MT", "연53"),
                        tuple(imageUrlGenerator.getImageUrl("abc52"), "MT", "연52"),
                        tuple(imageUrlGenerator.getImageUrl("abc51"), "MT", "연51"));

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", chatRooms.get(9).chatRoomId());
        assertThat(chatRooms2).extracting(
                        "partnerProfile.mainPhoto",
                        "partnerProfile.primaryRole",
                        "partnerProfile.nickName")
                .containsExactly(
                        tuple(imageUrlGenerator.getImageUrl("abc50"), "MT", "연50"),
                        tuple(imageUrlGenerator.getImageUrl("abc49"), "MT", "연49"),
                        tuple(imageUrlGenerator.getImageUrl("abc48"), "MT", "연48"),
                        tuple(imageUrlGenerator.getImageUrl("abc47"), "MT", "연47"),
                        tuple(imageUrlGenerator.getImageUrl("abc46"), "MT", "연46"),
                        tuple(imageUrlGenerator.getImageUrl("abc45"), "MT", "연45"),
                        tuple(imageUrlGenerator.getImageUrl("abc44"), "MT", "연44"),
                        tuple(imageUrlGenerator.getImageUrl("abc43"), "MT", "연43"),
                        tuple(imageUrlGenerator.getImageUrl("abc42"), "MT", "연42"),
                        tuple(imageUrlGenerator.getImageUrl("abc41"), "MT", "연41"));

        List<ChatRoomResponse> chatRooms3 = chatFacadeV1.getChatRooms("dusgh1234", chatRooms2.get(9).chatRoomId());
        assertThat(chatRooms3).extracting(
                        "partnerProfile.mainPhoto",
                        "partnerProfile.primaryRole",
                        "partnerProfile.nickName")
                .containsExactly(
                        tuple(imageUrlGenerator.getImageUrl("abc40"), "MT", "연40"),
                        tuple(imageUrlGenerator.getImageUrl("abc39"), "MT", "연39"),
                        tuple(imageUrlGenerator.getImageUrl("abc38"), "MT", "연38"),
                        tuple(imageUrlGenerator.getImageUrl("abc37"), "MT", "연37"),
                        tuple(imageUrlGenerator.getImageUrl("abc36"), "MT", "연36"),
                        tuple(imageUrlGenerator.getImageUrl("abc35"), "MT", "연35"),
                        tuple(imageUrlGenerator.getImageUrl("abc34"), "MT", "연34"),
                        tuple(imageUrlGenerator.getImageUrl("abc33"), "MT", "연33"),
                        tuple(imageUrlGenerator.getImageUrl("abc32"), "MT", "연32"),
                        tuple(imageUrlGenerator.getImageUrl("abc31"), "MT", "연31"));
    }


    @DisplayName("상대방이 채팅방을 나가면 채팅방에 정보에 표시된다")
    @Test
    void test4() throws InterruptedException {

        sendMessage("dusgh1234", "dusgh12345", "hello");
        sendMessage("dusgh1234", "dusgh12", "hi~");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh123", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms.size()).isEqualTo(3);

        assertThat(chatRooms.get(2).lastChat()).isEqualTo("hello");
        assertThat(chatRooms.get(2).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(2).partnerProfile().nickName()).isEqualTo("알렉2");

        assertThat(chatRooms.get(1).lastChat()).isEqualTo("hi~");
        assertThat(chatRooms.get(1).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(1).partnerProfile().nickName()).isEqualTo("연1");

        assertThat(chatRooms.get(0).lastChat()).isEqualTo("hi");
        assertThat(chatRooms.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
        assertThat(chatRooms.get(0).partnerProfile().nickName()).isEqualTo("연2");

        chatFacadeV1.leaveChatRoom(chatRooms.get(0).chatRoomId(), "dusgh1234");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12345", null);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRooms.get(0).chatRoomId()).get();

        assertThat(chatRoom.getExitedUserLoginId()).isEqualTo("dusgh1234");
        assertThat(chatRooms2).hasSize(1);

    }

    @DisplayName("이미 종료된 채팅방은 조회하지 못한다")
    @Test
    void test5() throws InterruptedException {

        sendMessage("dusgh1234", "dusgh12", "hi~");
        sendMessage("dusgh1234", "dusgh123", "hello");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh12345", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(1).chatRoomId(), "dusgh1234");
        chatFacadeV1.leaveChatRoom(chatRooms.get(1).chatRoomId(), "dusgh12");

        List<ChatRoomResponse> chatRooms1 = chatFacadeV1.getChatRooms("dusgh1234", null);
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);

        assertThat(chatRooms1.size()).isEqualTo(2);
        assertThat(chatRooms2).isEmpty();
    }

    @DisplayName("종료된 채팅방은 채팅 내역을 조회할 수 없다")
    @Test
    void test6() throws InterruptedException {

        sendMessage("dusgh1234", "dusgh12", "hi~");
        sendMessage("dusgh1234", "dusgh123", "hello");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh12345", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(1).chatRoomId(), "dusgh1234");
        chatFacadeV1.leaveChatRoom(chatRooms.get(1).chatRoomId(), "dusgh12");

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh1234", chatRooms.get(1).chatRoomId(), null)).isInstanceOf(CustomException.class).hasMessage("이미 종료된 채팅방입니다");

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh12", chatRooms.get(1).chatRoomId(), null)).isInstanceOf(CustomException.class).hasMessage("이미 종료된 채팅방입니다");
    }

    @DisplayName("상대방이 나간 채팅 내역을 조회할 수 없다")
    @Test
    void test7() throws InterruptedException {
        sendMessage("dusgh1234", "dusgh12", "hi~");
        sendMessage("dusgh1234", "dusgh123", "hello");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh12345", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(1).chatRoomId(), "dusgh12");
        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh1234", null);
        assertThat(chatRooms2.size()).isEqualTo(3);

        assertThatThrownBy(() -> chatFacadeV1.getChatList("dusgh1234", chatRooms.get(1).chatRoomId(), null)).isInstanceOf(CustomException.class).hasMessage("상대방이 채팅방을 떠났습니다");
    }

    @DisplayName("자신이 나간 채팅 내역을 조회할 수 없다")
    @Test
    void test8() throws InterruptedException {

        sendMessage("dusgh1234", "dusgh12", "hi~");
        sendMessage("dusgh1234", "dusgh123", "hello");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh12345", "hi");

        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms("dusgh1234", null);
        chatFacadeV1.leaveChatRoom(chatRooms.get(1).chatRoomId(), "dusgh12");

        List<ChatRoomResponse> chatRooms2 = chatFacadeV1.getChatRooms("dusgh12", null);
        assertThat(chatRooms.size()).isEqualTo(3);
        assertThat(chatRooms2.size()).isEqualTo(0);
    }

    @DisplayName("밴 당한 유저와의 채팅방은 조회되지 않는다")
    @Test
    void test9() throws InterruptedException {
        sendMessage("dusgh1234", "dusgh12", "hi~");
        sendMessage("dusgh1234", "dusgh123", "hello");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh12345", "hi");

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
    void test10() throws InterruptedException {

        sendMessage("dusgh1234", "dusgh12", "hi~");
        sendMessage("dusgh1234", "dusgh123", "hello");
        Thread.sleep(1000);
        sendMessage("dusgh1234", "dusgh12345", "hi");

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
        sendMessage("dusgh1234", "dusgh12", "hi~");
        IndexResponse index1 = indexServiceV1.refreshIndex("dusgh12");
        IndexResponse index2 = indexServiceV1.refreshIndex("dusgh1234");

        assertThat(index1.hasNewChat()).isTrue();
        assertThat(index2.hasNewChat()).isFalse();

    }

    @DisplayName("메시지를 조회하면 새로운 메시지 표시가 사라진다")
    @Test
    void test12() {

        sendMessage("dusgh1234", "dusgh12", "hi~");
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByUsers("dusgh1234", "dusgh12").get();
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


    private ChatWithMissingChat sendMessage(String sender, String receiver, String message) {
        ChatRequest msg = new ChatRequest(0, sender, receiver, message);
        LocalDateTime now = LocalDateTime.now();
        return chatServiceV1.saveChatMessage(msg, now);
    }

}