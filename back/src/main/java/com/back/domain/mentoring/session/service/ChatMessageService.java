package com.back.domain.mentoring.session.service;

import com.back.domain.mentoring.session.entity.ChatMessage;
import com.back.domain.mentoring.session.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage createChatMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }
}
