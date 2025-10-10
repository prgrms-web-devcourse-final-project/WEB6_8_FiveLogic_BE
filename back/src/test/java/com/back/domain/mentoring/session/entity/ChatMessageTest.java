package com.back.domain.mentoring.session.entity;


import com.back.domain.member.member.entity.Member;
import com.back.fixture.MemberFixture;
import com.back.fixture.mentoring.MentoringSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatMessageTest {
    @Test
    @DisplayName("ChatMessage 생성 테스트")
    void createChatMessageTest(){
        // given
        MentoringSession session = MentoringSessionFixture.createDefault();
        Member sender = MemberFixture.createDefault();
        String content = "Hello world";
        SenderRole senderRole = SenderRole.MENTOR;
        MessageType type = MessageType.TEXT;

        // when
        ChatMessage message = ChatMessage.create(session, sender, senderRole, content, type);

        // then
        assertThat(message.getMentoringSession()).isEqualTo(session);
        assertThat(message.getSender()).isEqualTo(sender);
        assertThat(message.getSenderRole()).isEqualTo(senderRole);
        assertThat(message.getContent()).isEqualTo(content);
        assertThat(message.getType()).isEqualTo(type);
        assertThat(message.getTimestamp()).isNotNull();
        assertThat(message.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("시스템 메시지 생성 테스트")
    void createSystemMessageTest() {
        // given
        MentoringSession session = MentoringSessionFixture.createDefault();
        String content = "멘토링 세션이 시작되었습니다.";
        MessageType type = MessageType.SYSTEM;
        SenderRole senderRole = SenderRole.SYSTEM;
        Member sender = null; // 시스템 메시지는 발신자가 없을 수 있습니다.

        // when
        ChatMessage message = ChatMessage.create(session, sender, senderRole, content, type);

        // then
        assertThat(message.getMentoringSession()).isEqualTo(session);
        assertThat(message.getSender()).isNull();
        assertThat(message.getSenderRole()).isEqualTo(senderRole);
        assertThat(message.getContent()).isEqualTo(content);
        assertThat(message.getType()).isEqualTo(type);
        assertThat(message.getTimestamp()).isNotNull();
        assertThat(message.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Nested
    @DisplayName("ChatMessage 생성 유효성 검증")
    class ValidationTests {

        @Test
        @DisplayName("MentoringSession이 null이면 예외가 발생한다")
        void createWithNullSession_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(null, MemberFixture.createDefault(), SenderRole.MENTOR, "message", MessageType.TEXT));
        }

        @Test
        @DisplayName("Content가 null이면 예외가 발생한다")
        void createWithNullContent_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), MemberFixture.createDefault(), SenderRole.MENTOR, null, MessageType.TEXT));
        }

        @Test
        @DisplayName("Content가 비어있으면 예외가 발생한다")
        void createWithBlankContent_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), MemberFixture.createDefault(), SenderRole.MENTOR, "   ", MessageType.TEXT));
        }

        @Test
        @DisplayName("MessageType이 null이면 예외가 발생한다")
        void createWithNullType_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), MemberFixture.createDefault(), SenderRole.MENTOR, "message", null));
        }

        @Test
        @DisplayName("시스템 메시지에 sender가 있으면 예외가 발생한다")
        void createSystemMessageWithSender_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), MemberFixture.createDefault(), SenderRole.SYSTEM, "system message", MessageType.SYSTEM));
        }

        @Test
        @DisplayName("시스템 메시지의 senderRole이 SYSTEM이 아니면 예외가 발생한다")
        void createSystemMessageWithInvalidRole_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), null, SenderRole.MENTOR, "system message", MessageType.SYSTEM));
        }

        @Test
        @DisplayName("일반 메시지에 sender가 없으면 예외가 발생한다")
        void createUserMessageWithNullSender_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), null, SenderRole.MENTOR, "user message", MessageType.TEXT));
        }

        @Test
        @DisplayName("일반 메시지의 senderRole이 MENTOR나 MENTEE가 아니면 예외가 발생한다")
        void createUserMessageWithInvalidRole_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () ->
                    ChatMessage.create(MentoringSessionFixture.createDefault(), MemberFixture.createDefault(), SenderRole.SYSTEM, "user message", MessageType.TEXT));
        }
    }
}