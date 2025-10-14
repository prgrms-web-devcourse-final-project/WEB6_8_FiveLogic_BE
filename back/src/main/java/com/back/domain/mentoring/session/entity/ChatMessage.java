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
        if (mentoringSession == null) {
            throw new IllegalArgumentException("MentoringSession은 null일 수 없습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content는 null이거나 비어 있을 수 없습니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("MessageType은 null일 수 없습니다.");
        }

        if (type == MessageType.SYSTEM) {
            if (sender != null) {
                throw new IllegalArgumentException("시스템 메시지의 sender는 null이어야 합니다.");
            }
            if (senderRole != SenderRole.SYSTEM) {
                throw new IllegalArgumentException("시스템 메시지의 senderRole은 SYSTEM이어야 합니다.");
            }
        } else { // TEXT, IMAGE, FILE
            if (sender == null) {
                throw new IllegalArgumentException("일반 메시지의 sender는 null일 수 없습니다.");
            }
            if (senderRole != SenderRole.MENTOR && senderRole != SenderRole.MENTEE) {
                throw new IllegalArgumentException("일반 메시지의 senderRole은 MENTOR 또는 MENTEE여야 합니다.");
            }

            boolean isParticipant = (mentoringSession.getReservation().getMentor().isMember(sender) ||
                    mentoringSession.getReservation().getMentee().isMember(sender));

            if (!isParticipant) {
                throw new IllegalArgumentException("메시지 발신자는 해당 멘토링 세션의 참여자가 아닙니다.");
            }
        }

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
