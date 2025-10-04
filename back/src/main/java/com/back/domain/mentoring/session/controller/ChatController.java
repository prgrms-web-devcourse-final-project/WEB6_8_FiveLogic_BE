package com.back.domain.mentoring.session.controller;

import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping({"/chat/message/{mentoringSessionId}"})
    public void message(@DestinationVariable String mentoringSessionId, ChatMessageRequest message, @AuthenticationPrincipal Principal principal) {
        log.info("사용자 정보 : " + principal);
        messagingTemplate.convertAndSend("/topic/chat/room/" + mentoringSessionId, message);
    }
}
