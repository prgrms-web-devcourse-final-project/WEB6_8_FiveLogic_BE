package com.back.domain.mentoring.session.controller;

import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.service.ChatManager;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatManager chatManager;
    private final Rq rq;

    @MessageMapping({"/chat/message/{mentoringSessionUuid}"})
    public void message(@DestinationVariable String mentoringSessionUuid, Principal principal, ChatMessageRequest chatMessageRequest) {

        chatManager.chat(mentoringSessionUuid, principal, chatMessageRequest);
    }
}
