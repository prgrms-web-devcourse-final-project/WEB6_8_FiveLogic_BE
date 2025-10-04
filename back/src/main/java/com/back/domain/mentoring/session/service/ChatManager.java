package com.back.domain.mentoring.session.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.dto.ChatMessageResponse;
import com.back.domain.mentoring.session.entity.MentoringSession;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatManager {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final MentoringStorage mentoringStorage;

    @Transactional
    public void chat(String mentoringSessionId, Member member, ChatMessageRequest chatMessageRequest) {
        MentoringSession mentoringSession = mentoringStorage.getMentoringSessionBySessionUuid(mentoringSessionId);
        ChatMessageResponse responseDto = chatMessageService.saveAndProcessMessage(member, mentoringSession, chatMessageRequest);

        messagingTemplate.convertAndSend("/topic/chat/room/" + mentoringSessionId, responseDto);
    }
}
