package com.plaything.api.domain.chat.controller;

import com.plaything.api.domain.chat.model.response.ChatList;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.chat.service.ChatFacadeV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat API", description = "V1 Chat API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatControllerV1 {

    private final ChatFacadeV1 chatFacadeV1;

    @Operation(
            summary = "매칭된 대상들의 채팅방 목록을 조회합니다.",
            description = """
                    매칭된 상대 10명과의 채팅방을 조회합니다.
                    채팅방 목록의 id 중 가장 작은 것을 보내면 추가로 채팅방을 조회하게 됩니다.
                    처음 보낼 땐 lastId를 null로 보냅니다.
                    """
    )
    @GetMapping("/chat-rooms")
    public List<ChatRoomResponse> chatRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "lastId", required = false) Long lastChatRoomId

    ) {
        return chatFacadeV1.getChatRooms(userDetails.getUsername(), lastChatRoomId);
    }

    @Operation(
            summary = "채팅 내역 10개를 추가로 조회합니다",
            description = """
                    채팅방에서 이전 채팅 내역 10개를 가져옵니다.
                    lastChatId는 현재 갖고 있는 채팅 메시지 id중 가장 작은 걸 보냅니다.
                    가장 최근 채팅 목록을 가져올 땐 lastChatId를 null로 보냅니다.
                    
                    #예외#
                    (1) 상대방이 나갔을 때 api를 호출하면 '상대방이 나갔다'는 예외
                    (2) 종료된 채팅방은 '이미 종료된 채팅방'라는 예외
                    """

    )
    @GetMapping("/chat-list/{chatRoomId}")
    public ChatList chatList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(value = "lastChatId", required = false) Long lastChatId

    ) {
        return chatFacadeV1.getChatList(userDetails.getUsername(), chatRoomId, lastChatId);
    }

    @Operation(
            summary = "채팅방을 나갑니다",
            description = """
                    채팅방과 함께 최근 10개의 대화 내역을 가져옵니다.
                    URL에 채팅방 id를 보냅니다.
                    """
    )
    @PutMapping("/leave-chatroom/{id}")
    public void leaveChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id

    ) {

        String user = userDetails.getUsername();
        chatFacadeV1.leaveChatRoom(id, user);
    }
}
