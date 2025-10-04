package com.back.domain.mentoring.session.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.service.ChatManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatManager chatManager;

    @MessageMapping({"/chat/message/{mentoringSessionUuid}"})
    public void message(@DestinationVariable String mentoringSessionUuid, @AuthenticationPrincipal Member member, ChatMessageRequest chatMessageRequest) {
        chatManager.chat(mentoringSessionUuid, member, chatMessageRequest);
    }
}
