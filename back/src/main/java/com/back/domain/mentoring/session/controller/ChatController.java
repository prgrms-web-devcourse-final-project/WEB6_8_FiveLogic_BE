package com.back.domain.mentoring.session.controller;

import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/message/{mentoringSessionId}")
    public void message(@DestinationVariable String mentoringSessionId, ChatMessageRequest message) {
        messagingTemplate.convertAndSend("/topic/chat/room/" + mentoringSessionId, message);
    }
}
