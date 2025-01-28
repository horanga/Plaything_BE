package com.plaything.api.domain.chat.handler;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(ex.getMessage());
        accessor.setLeaveMutable(true);

        StompHeaderAccessor clientHeaderAccessor = null;
        if (clientMessage != null) {
            clientHeaderAccessor = MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);
            if (clientHeaderAccessor != null) {
                String receiptId = clientHeaderAccessor.getReceipt();
                if (receiptId != null) {
                    accessor.setReceiptId(receiptId);
                }
            }
        }

        return handleInternal(accessor, EMPTY_PAYLOAD, ex, clientHeaderAccessor);
    }

    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor, byte[] errorPayload,
                                             Throwable cause, StompHeaderAccessor clientHeaderAccessor) {

        String errorCause = "";
        if (cause != null) {
            CustomException customException = (CustomException) cause.getCause();
            errorCause = ErrorResponse.toResponse(customException).toString();

        }
        byte[] newPayload = errorCause.getBytes(StandardCharsets.UTF_8);
        return MessageBuilder.createMessage(newPayload, errorHeaderAccessor.getMessageHeaders());
    }

}
