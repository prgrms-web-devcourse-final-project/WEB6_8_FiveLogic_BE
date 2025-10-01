package com.back.domain.mentoring.session.entity;

import com.back.domain.member.member.entity.Member;
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

    @ManyToOne
    private Member sender;

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
    private ChatMessage(MentoringSession mentoringSession, Member sender, SenderRole senderRole, String content, MessageType type, LocalDateTime timestamp) {
        this.mentoringSession = mentoringSession;
        this.sender = sender;
        this.senderRole = senderRole;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public static ChatMessage create(MentoringSession mentoringSession, Member sender, SenderRole senderRole, String content, MessageType type) {
        return ChatMessage.builder()
                .mentoringSession(mentoringSession)
                .sender(sender)
                .senderRole(senderRole)
                .content(content)
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
