package com.back.domain.mentoring.session.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_session_id", nullable = false)
    private MentoringSession mentoringSession;

    @Column
    private Long senderId;

    @Column
    @Enumerated(EnumType.STRING)
    private SenderRole senderRole;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private ChatMessage(MentoringSession mentoringSession, Long senderId, SenderRole senderRole, String content, MessageType type, LocalDateTime timestamp) {
        this.mentoringSession = mentoringSession;
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public static ChatMessage create(MentoringSession mentoringSession, Long senderId, SenderRole senderRole, String content, MessageType type) {
        return ChatMessage.builder()
                .mentoringSession(mentoringSession)
                .senderId(senderId)
                .senderRole(senderRole)
                .content(content)
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
