package com.back.domain.mentoring.session.controller;

import com.back.domain.mentoring.session.dto.WebRtcSignalingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebRtcController {

    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/signal")
    public void handleSignalingMessage(WebRtcSignalingMessage message, Principal principal) {
        String senderUsername = principal.getName();

        WebRtcSignalingMessage finalMessage = new WebRtcSignalingMessage(message.type(), senderUsername, message.to(), message.sessionId(), message.payload());

        log.debug("[Signal] type: {}, from: {}, to: {}, sessionId: {}",
                finalMessage.type(), finalMessage.from(), finalMessage.to(), finalMessage.sessionId());

        // 'to' 필드가 있으면 특정 사용자에게, 없으면 세션 전체에 브로드캐스트
        if (finalMessage.to() != null && !finalMessage.to().isEmpty()) {
            String userSpecificTopic = "/topic/signal/user/" + finalMessage.to();
            messagingTemplate.convertAndSend(userSpecificTopic, finalMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/signal/room/" + finalMessage.sessionId(), finalMessage);
        }
    }
}
