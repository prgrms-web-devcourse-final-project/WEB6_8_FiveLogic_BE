package com.back.domain.mentoring.session.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.dto.ChatMessageResponse;
import com.back.domain.mentoring.session.entity.ChatMessage;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.session.entity.MessageType;
import com.back.domain.mentoring.session.entity.SenderRole;
import com.back.domain.mentoring.session.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage create(MentoringSession mentoringSession, Member sender, String content, MessageType messageType) {
        SenderRole senderRole = sender.getRole() == Member.Role.MENTOR ? SenderRole.MENTOR : SenderRole.MENTEE;
        ChatMessage chatMessage = ChatMessage.create(mentoringSession, sender, senderRole , content, messageType);
        return chatMessageRepository.save(chatMessage);
    }

    public ChatMessageResponse saveAndProcessMessage(Member sender, MentoringSession mentoringSession, ChatMessageRequest messageRequest) {
        ChatMessage message = create(mentoringSession, sender, messageRequest.content(), messageRequest.type());

        return new ChatMessageResponse(
                message.getSender().getNickname(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}