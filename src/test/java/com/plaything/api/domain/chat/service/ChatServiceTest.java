package com.plaything.api.domain.chat.service;

import com.plaything.api.util.UserGenerator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


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
    private UserGenerator userGenerator;

    @AfterEach
    void cleanUp() {
        rateLimiter.cleanupOldData();
    }

    @BeforeEach
    void setUp() {
        userGenerator.generate("dusgh1234", "1234", "1", "알렉1");
        userGenerator.addImages("알렉1", "abc", true);

        userGenerator.generate("dusgh12345", "1234", "1", "알렉2");
        userGenerator.addImages("알렉2", "abcd", true);

        userGenerator.createMatching("dusgh1234", "dusgh12345");
    }

//
//    @DisplayName("채팅 메시지 내역을 조회할 수 있다.")
//    @Test
//    void test1() throws InterruptedException {
//
//        LocalDateTime now = LocalDateTime.now();
//
//
//        sendMessage("dusgh1234", "dusgh12345", "안녕", 1, now);
//        sendMessage("dusgh1234", "dusgh12345", "반가워", 2, now);
//        Thread.sleep(300);
//        sendMessage("dusgh1234", "dusgh12345", "뭐해?", 3, now);
//
//        sendMessage("dusgh12345", "dusgh1234", "하이", 4, now);
//        sendMessage("dusgh12345", "dusgh1234", "그냥 쉬는중ㅋㅋ", 5, now);
//
//        List<ChatRoom> list = chatFacadeV1.getChatRooms("dusgh1234", null);
//
//        assertThat(list.get(0).partnerProfile().nickName()).isEqualTo("알렉2");
//        assertThat(list.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
//        assertThat(list.get(0).partnerProfile().mainPhoto()).isEqualTo("https://d25ulpahxovik9.cloudfront.net/abcd");
//        assertThat(list.get(0).lastChat()).isEqualTo("그냥 쉬는중ㅋㅋ");
//        assertThat(list.get(0).lastChatSender()).isEqualTo("dusgh12345");
//
//
//        ChatList list = chatFacadeV1.getChatList("dusgh1234", list.get(0).chatRoomId(), null);
//        assertThat(list.list()).extracting("list")
//                .containsExactly("그냥 쉬는중ㅋㅋ", "하이", "뭐해?", "반가워", "안녕");
//
//        assertThat(list.list()).extracting("sequence")
//                .containsExactly(5, 4, 3, 2, 1);
//
//        assertThat(list.list()).extracting("senderLoginId")
//                .containsExactly("dusgh12345", "dusgh12345", "dusgh1234", "dusgh1234", "dusgh1234");
//    }
//
//    @DisplayName("채팅 메시지를 페이지네이션 할 수 있다")
//    @Test
//    void test2() throws InterruptedException {
//        LocalDateTime now = LocalDateTime.now();
//
//        for (int i = 1; i <= 20; i++) {
//            sendMessage("dusgh1234", "dusgh12345", "시작" + i, i, now);
//            Thread.sleep(300);
//        }
//
//
//        sendMessage("dusgh1234", "dusgh12345", "안녕", 1, now);
//        sendMessage("dusgh1234", "dusgh12345", "반가워", 2, now);
//        Thread.sleep(300);
//        sendMessage("dusgh1234", "dusgh12345", "뭐해?", 3, now);
//
//        sendMessage("dusgh12345", "dusgh1234", "하이", 4, now);
//        sendMessage("dusgh12345", "dusgh1234", "그냥 쉬는중ㅋㅋ", 5, now);
//
//        List<ChatRoom> list = chatFacadeV1.getChatRooms("dusgh1234", null);
//
//        assertThat(list.get(0).partnerProfile().nickName()).isEqualTo("알렉2");
//        assertThat(list.get(0).partnerProfile().primaryRole()).isEqualTo("MT");
//        assertThat(list.get(0).partnerProfile().mainPhoto()).isEqualTo("https://d25ulpahxovik9.cloudfront.net/abcd");
//        assertThat(list.get(0).lastChat()).isEqualTo("그냥 쉬는중ㅋㅋ");
//        assertThat(list.get(0).lastChatSender()).isEqualTo("dusgh12345");
//
//
//        ChatList list = chatFacadeV1.getChatList("dusgh1234", list.get(0).chatRoomId(), null);
//        assertThat(list.list()).hasSize(10);
//        assertThat(list.list()).extracting("list")
//                .containsExactly("그냥 쉬는중ㅋㅋ", "하이", "뭐해?", "반가워", "안녕", "시작20", "시작19", "시작18", "시작17", "시작16");
//
//        assertThat(list.list()).extracting("sequence")
//                .containsExactly(25, 24, 23, 22, 21, 20, 19, 18, 17, 16);
//
//        assertThat(list.list()).extracting("senderLoginId")
//                .containsExactly(
//                        "dusgh12345",
//                        "dusgh12345",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234");
//
//
//        ChatList chatList2 = chatFacadeV1.getChatList("dusgh1234", list.get(0).chatRoomId(), list.list().get(9).id());
//
//        assertThat(chatList2.list()).hasSize(10);
//        assertThat(chatList2.list()).extracting("list")
//                .containsExactly("시작15", "시작14", "시작13", "시작12", "시작11", "시작10", "시작9", "시작8", "시작7", "시작6");
//
//        assertThat(chatList2.list()).extracting("sequence")
//                .containsExactly(15, 14, 13, 12, 11, 10, 9, 8, 7, 6);
//
//        assertThat(chatList2.list()).extracting("senderLoginId")
//                .containsExactly(
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234");
//
//
//        ChatList chatList3 = chatFacadeV1.getChatList("dusgh1234", list.get(0).chatRoomId(), chatList2.list().get(9).id());
//
//        assertThat(chatList3.list()).hasSize(5);
//        assertThat(chatList3.list()).extracting("list")
//                .containsExactly("시작5", "시작4", "시작3", "시작2", "시작1");
//
//        assertThat(chatList3.list()).extracting("sequence")
//                .containsExactly(5, 4, 3, 2, 1);
//
//        assertThat(chatList3.list()).extracting("senderLoginId")
//                .containsExactly(
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234");
//
//    }
//
//    @DisplayName("채팅방 내용은 최대 일주일전까지만 볼 수 있다.")
//    @Test
//    void test3(
//    ) {
//        LocalDateTime date1 = LocalDateTime.now().minusDays(8);
//
//        for (int i = 1; i <= 25; i++) {
//            sendMessage("dusgh1234", "dusgh12345", "하이" + i, i, date1);
//        }
//
//        LocalDateTime date2 = LocalDateTime.now().minusDays(7);
//
//        for (int i = 26; i <= 30; i++) {
//            sendMessage("dusgh1234", "dusgh12345", "하이" + i, i, date2);
//
//        }
//
//        List<ChatRoom> list = chatFacadeV1.getChatRooms("dusgh1234", null);
//
//        ChatList list = chatFacadeV1.getChatList("dusgh1234", list.get(0).chatRoomId(), null);
//        assertThat(list.list()).hasSize(5);
//        assertThat(list.list()).extracting("list")
//                .containsExactly("하이30", "하이29", "하이28", "하이27", "하이26");
//
//        assertThat(list.list()).extracting("sequence")
//                .containsExactly(30, 29, 28, 27, 26);
//
//        assertThat(list.list()).extracting("senderLoginId")
//                .containsExactly("dusgh1234", "dusgh1234", "dusgh1234", "dusgh1234", "dusgh1234");
//
//        assertThat(list.list()).extracting("createdAt")
//                .containsExactly(date2, date2, date2, date2, date2);
//    }
//
//    @DisplayName("채팅 메시지를 보낼 때 누락된 메시지가 있으면 이를 받게 된다")
//    @Test
//    void test4() {
//        LocalDateTime date1 = LocalDateTime.now();
//
//        for (int i = 1; i <= 3; i++) {
//            ChatWithMissingChat chatWithMissingChat = sendMessage("dusgh1234", "dusgh12345", "하이" + i, i, date1);
//
//            assertThat(chatWithMissingChat.missingChat()).isEmpty();
//            assertThat(chatWithMissingChat.chat().chat()).isEqualTo("하이" + i);
//            assertThat(chatWithMissingChat.chat().receiverLoginId()).isEqualTo("dusgh12345");
//            assertThat(chatWithMissingChat.chat().senderLoginId()).isEqualTo("dusgh1234");
//            assertThat(chatWithMissingChat.chat().createdAt()).isEqualTo(date1);
//            assertThat(chatWithMissingChat.chat().sequence()).isEqualTo(i);
//        }
//
//
//        ChatWithMissingChat chatWithMissingChat = sendMessage("dusgh12345", "dusgh1234", "하이~", 1, date1);
//
//        assertThat(chatWithMissingChat.missingChat()).extracting("chat").containsExactly("하이2", "하이3");
//        assertThat(chatWithMissingChat.missingChat()).extracting("senderLoginId").containsExactly("dusgh1234", "dusgh1234");
//        assertThat(chatWithMissingChat.missingChat()).extracting("receiverLoginId").containsExactly("dusgh12345", "dusgh12345");
//        assertThat(chatWithMissingChat.missingChat()).extracting("createdAt").containsExactly(date1, date1);
//        assertThat(chatWithMissingChat.missingChat()).extracting("sequence").containsExactly(2, 3);
//
//        assertThat(chatWithMissingChat.chat().chat()).isEqualTo("하이~");
//        assertThat(chatWithMissingChat.chat().receiverLoginId()).isEqualTo("dusgh1234");
//        assertThat(chatWithMissingChat.chat().senderLoginId()).isEqualTo("dusgh12345");
//        assertThat(chatWithMissingChat.chat().createdAt()).isEqualTo(date1);
//        assertThat(chatWithMissingChat.chat().sequence()).isEqualTo(4);
//
//    }
//
//
//    @DisplayName("동시성 테스트")
//    @Test
//    void test5() {
//        LocalDateTime date1 = LocalDateTime.now();
//
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        CountDownLatch countDownLatch = new CountDownLatch(10);
//
//        for (int i = 0; i < 10; i++) {
//            final int index = i;
//            executorService.submit(() -> {
//                try {
//                    sendMessage("dusgh1234", "dusgh12345", "하이" + index, index, date1);
//                } finally {
//                    countDownLatch.countDown(); // 작업 완료 시 카운트 감소
//                }
//            });
//        }
//
//        try {
//            countDownLatch.await(); // 모든 작업이 완료될 때까지 대기
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        executorService.shutdown();
//        List<ChatRoom> list = chatFacadeV1.getChatRooms("dusgh1234", null);
//
//        ChatList list = chatFacadeV1.getChatList("dusgh1234", list.get(0).chatRoomId(), null);
//        assertThat(list.list()).hasSize(5);
//        assertThat(list.list()).extracting("list")
//                .containsExactly("하이10", "하이9", "하이8", "하이7", "하이6", "하이5", "하이4", "하이3", "하이2", "하이1");
//
//        assertThat(list.list()).extracting("sequence")
//                .containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
//
//        assertThat(list.list()).extracting("senderLoginId")
//                .containsExactly(
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234",
//                        "dusgh1234"
//                );
//    }
//
//
//    private ChatWithMissingChat sendMessage(String senderLoginId, String reciverLoginId, String list, int sequence, LocalDateTime now) {
//        ChatRequest msg = new ChatRequest(sequence, senderLoginId, reciverLoginId, list);
//        return chatServiceV1.saveChatMessage(msg, now);
//    }

}
