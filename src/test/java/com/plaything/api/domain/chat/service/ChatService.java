package com.plaything.api.domain.chat.service;

public class ChatService {

    //
//    @DisplayName("이전 채팅 목록을 조회할 수 있다.")
//    @ParameterizedTest
//    @MethodSource("chatProvider2")
//    void test2(
//            String sender,
//            String receiver,
//            String senderLoginId,
//            String partnerNickname,
//            String partnerRole,
//            String mainPhotoUrl,
//            List<String> senders,
//            String requesterLoginId) {
//
//        Long lastId1 = 0L;
//        Long lastId2 = 0L;
//        LocalDateTime now = LocalDateTime.now();
//
//        for (int i = 1; i <= 30; i++) {
//            Message msg = new Message(sender, receiver, "안녕" + i);
//            Chat chat = chatServiceV1.saveChatMessage(msg, now);
//            if (i == 21) {
//                lastId1 = chat.getId();
//                continue;
//            }
//
//            if (i == 11) {
//                lastId2 = chat.getId();
//            }
//        }
//
//        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, lastId1, LocalDate.now());
//
//        assertThat(chatRooms.get(0).chatProfile().nickName()).isEqualTo(partnerNickname);
//        assertThat(chatRooms.get(0).chatProfile().primaryRole()).isEqualTo(partnerRole);
//        assertThat(chatRooms.get(0).chatProfile().mainPhoto()).isEqualTo(mainPhotoUrl);
//
//        ChatListResponse chatListResponse = chatRooms.get(0).chatList();
//        assertThat(chatListResponse.message()).extracting("message").containsExactly(
//                "안녕20",
//                "안녕19",
//                "안녕18",
//                "안녕17",
//                "안녕16",
//                "안녕15",
//                "안녕14",
//                "안녕13",
//                "안녕12",
//                "안녕11"
//        );
//
//        assertThat(chatListResponse.message()).extracting("sendNickname").containsExactlyElementsOf(senders);
//
//        ChatListResponse chatList = chatFacadeV1.getChatList(requesterLoginId, chatRooms.get(0).chatRoomId(), lastId2);
//
//        assertThat(chatList.message()).extracting("message").containsExactly(
//                "안녕10",
//                "안녕9",
//                "안녕8",
//                "안녕7",
//                "안녕6",
//                "안녕5",
//                "안녕4",
//                "안녕3",
//                "안녕2",
//                "안녕1"
//        );
//    }
//
//    public static Stream<Arguments> chatProvider2() {
//        List<String> sender = Collections.nCopies(10, "알렉1");
//        List<String> sender2 = Collections.nCopies(10, "알렉2");
//        return Stream.of(
//                Arguments.of("알렉1",
//                        "알렉2",
//                        "dusgh1234",
//                        "알렉2",
//                        "MT",
//                        "abcd",
//                        sender,
//                        "dusgh1234"
//                ),
//                Arguments.of("알렉2",
//                        "알렉1",
//                        "dusgh12345",
//                        "알렉1",
//                        "MT",
//                        "abc",
//                        sender2,
//                        "dusgh12345"
//                ));
//    }
//
//
//    @DisplayName("채팅방 내용은 최대 일주일전까지만 볼 수 있다.")
//    @ParameterizedTest
//    @MethodSource("chatProvider3")
//    void test3(
//            String sender,
//            String receiver,
//            String senderLoginId,
//            String partnerNickname,
//            String partnerRole,
//            String mainPhotoUrl
//    ) {
//        LocalDateTime date1 = LocalDateTime.now().minusDays(8);
//
//        for (int i = 1; i <= 20; i++) {
//            Message msg = new Message(sender, receiver, "안녕" + i);
//            chatServiceV1.saveChatMessage(msg, date1);
//        }
//
//        LocalDateTime date2 = LocalDateTime.now().minusDays(7);
//        Long lastId1 = 0L;
//        for (int i = 1; i <= 20; i++) {
//            Message msg = new Message(sender, receiver, "안녕" + (i + 20));
//            Chat chat = chatServiceV1.saveChatMessage(msg, date2);
//            if (i == 5) {
//                lastId1 = chat.getId();
//            }
//        }
//
//        List<ChatRoomResponse> chatRooms = chatFacadeV1.getChatRooms(senderLoginId, lastId1, LocalDate.now());
//
//        assertThat(chatRooms.get(0).chatProfile().nickName()).isEqualTo(partnerNickname);
//        assertThat(chatRooms.get(0).chatProfile().primaryRole()).isEqualTo(partnerRole);
//        assertThat(chatRooms.get(0).chatProfile().mainPhoto()).isEqualTo(mainPhotoUrl);
//
//        ChatListResponse chatListResponse = chatRooms.get(0).chatList();
//
//        assertThat(chatListResponse.message()).extracting("message").containsExactly(
//                "안녕24",
//                "안녕23",
//                "안녕22",
//                "안녕21"
//        );
//    }
//
//
//    public static Stream<Arguments> chatProvider3() {
//        return Stream.of(
//                Arguments.of("알렉1",
//                        "알렉2",
//                        "dusgh1234",
//                        "알렉2",
//                        "MT",
//                        "abcd"
//                ),
//                Arguments.of("알렉2",
//                        "알렉1",
//                        "dusgh12345",
//                        "알렉1",
//                        "MT",
//                        "abc"
//                ));
//    }
//
}
