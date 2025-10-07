package com.back.domain.mentoring.session.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.dto.ChatMessageResponse;
import com.back.domain.mentoring.session.entity.MentoringSession;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class ChatManager {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final MentoringStorage mentoringStorage;
    private final MemberStorage memberStorage;

    public void chat(String mentoringSessionId, Principal principal, ChatMessageRequest chatMessageRequest) {
        ChatMessageResponse responseDto = saveChatMessage(mentoringSessionId, principal, chatMessageRequest);
        messagingTemplate.convertAndSend("/topic/chat/room/" + mentoringSessionId, responseDto);
    }
    @Transactional
    public ChatMessageResponse saveChatMessage(String mentoringSessionId, Principal principal, ChatMessageRequest chatMessageRequest) {
        Member member = memberStorage.findMemberByEmail(principal.getName());
        MentoringSession mentoringSession = mentoringStorage.getMentoringSessionBySessionUuid(mentoringSessionId);
        return chatMessageService.saveAndProcessMessage(member, mentoringSession, chatMessageRequest);
    }
}
