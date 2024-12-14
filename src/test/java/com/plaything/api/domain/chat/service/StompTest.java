package com.plaything.api.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.index.service.IndexServiceV1;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.repo.chat.ChatRepository;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.repository.repo.monitor.ProfileRecordRepository;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.security.JWTProvider;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.plaything.api.domain.user.constants.Gender.M;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StompTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private String url;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private IndexServiceV1 indexServiceV1;

    @Autowired
    private ProfileRecordRepository profileRecordRepository;


    @BeforeEach
    void setup() {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "alex", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);

        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "alex2", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        ChatRoom room = ChatRoom.builder().senderNickname("alex").receiverNickname("alex2").build();
        chatRoomRepository.save(room);

        Matching matching = Matching.builder().receiverNickname("alex").senderNickname("alex2").isMatched(true).isOvered(false).build();
        matchingRepository.save(matching);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        //LocalDateTime을 위한 모듈
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        stompClient.setMessageConverter(messageConverter);

        url = String.format("ws://localhost:%d/ws-stomp", port);


    }

    @AfterEach
    void cleanUpData() {
        chatRepository.deleteAll();
        chatRoomRepository.deleteAll();
        matchingRepository.deleteAll();
        // profile_record는 user를 참조하므로 먼저 삭제
        profileRecordRepository.deleteAll();  // profile_record 레포지토리 추가 필요
        // user는 profile을 참조하므로 user 먼저 삭제
        userRepository.deleteAll();
        // 마지막으로 profile 삭제
        profileRepository.deleteAll();

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @DisplayName("매칭된 상대방에게만 메시지를 보낼 수 있다")
    @Test
    void test1() throws ExecutionException, InterruptedException, TimeoutException {

        StompHeaders connectHeaders = new StompHeaders();
        String token = JWTProvider.createToken("dusgh1234");
        connectHeaders.add("Authorization", "Bearer " + token);

        StompSession receiverSession = stompClient.connect(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(10, TimeUnit.SECONDS);

        // 메시지 구독 설정
        CompletableFuture<ChatRequest> messageFuture = new CompletableFuture<>();
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/user/alex/chat");
        subscribeHeaders.add("Authorization", "Bearer " + token);

        receiverSession.subscribe(subscribeHeaders, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatRequest.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((ChatRequest) payload);
            }
        });

        StompHeaders connectHeaders2 = new StompHeaders();
        String token2 = JWTProvider.createToken("dusgh12345");
        connectHeaders2.add("Authorization", "Bearer " + token2);
        // 두 번째 세션 (수신자)
        StompSession senderSession = stompClient.connect(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders2,
                new StompSessionHandlerAdapter() {
                }
        ).get(15, TimeUnit.SECONDS);

        // when
        ChatRequest testChatRequest = new ChatRequest(1, "발신자", "alex", "test chat");
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/pub/chat/chat/alex");
        sendHeaders.add("Authorization", "Bearer " + token2);

        senderSession.send(sendHeaders, testChatRequest);

        // then
        ChatRequest receivedChatRequest = messageFuture.get(10, TimeUnit.SECONDS);
        assertThat(receivedChatRequest.chat()).isEqualTo("test chat");
        assertThat(receivedChatRequest.senderNickname()).isEqualTo("발신자");
        assertThat(receivedChatRequest.receiverNickname()).isEqualTo("alex");

    }

    @DisplayName("상대방에게서 메시지가 오면, 채팅방에 새로운 메시지가 왔다고 뜬다")
    @Test
    void test2() throws ExecutionException, InterruptedException, TimeoutException {

        StompHeaders connectHeaders = new StompHeaders();
        String token = JWTProvider.createToken("dusgh1234");
        connectHeaders.add("Authorization", "Bearer " + token);

        StompSession senderSession = stompClient.connect(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                }
        ).get(10, TimeUnit.SECONDS);

        StompHeaders connectHeaders2 = new StompHeaders();
        String token2 = JWTProvider.createToken("dusgh12345");
        connectHeaders2.add("Authorization", "Bearer " + token2);
        // 두 번째 세션 (수신자)
        StompSession receiverSession = stompClient.connect(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders2,
                new StompSessionHandlerAdapter() {
                }
        ).get(15, TimeUnit.SECONDS);

        CompletableFuture<ChatRequest> messageFuture = new CompletableFuture<>();
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/user/alex2/chat");
        subscribeHeaders.add("Authorization", "Bearer " + token2);

        receiverSession.subscribe(subscribeHeaders, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatRequest.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageFuture.complete((ChatRequest) payload);
            }
        });

        // when
        ChatRequest testChatRequest = new ChatRequest(1, "alex", "alex2", "test chat");
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/pub/chat/chat/alex2");
        sendHeaders.add("Authorization", "Bearer " + token);

        senderSession.send(sendHeaders, testChatRequest);

        Thread.sleep(5000);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        // then
        IndexResponse noti1 = indexServiceV1.refreshIndex("dusgh1234");
        IndexResponse noti2 = indexServiceV1.refreshIndex("dusgh12345");

        assertThat(noti1.hasNewChat()).isFalse();
        assertThat(noti2.hasNewChat()).isTrue();

    }

}
