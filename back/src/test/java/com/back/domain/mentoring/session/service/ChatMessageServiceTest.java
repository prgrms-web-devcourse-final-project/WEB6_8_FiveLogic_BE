package com.back.domain.mentoring.session.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.dto.ChatMessageRequest;
import com.back.domain.mentoring.session.dto.ChatMessageResponse;
import com.back.domain.mentoring.session.entity.ChatMessage;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.session.entity.MessageType;
import com.back.domain.mentoring.session.repository.ChatMessageRepository;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.mentoring.MentoringSessionFixture;
import com.back.fixture.mentoring.ReservationFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private Member menteeMember;
    private MentoringSession mentoringSession;

    @BeforeEach
    void setUp() {
        // Reservation에 포함된 실제 멘티 멤버를 가져와서 사용
        Reservation reservation = ReservationFixture.createDefault();
        menteeMember = reservation.getMentee().getMember();
        mentoringSession = MentoringSessionFixture.create(reservation);
    }

    @Test
    @DisplayName("채팅 메시지를 생성하고 저장한다.")
    void createAndSaveChatMessage() {
        // given
        String content = "안녕하세요!";
        MessageType messageType = MessageType.TEXT;
        ChatMessage chatMessage = ChatMessage.create(mentoringSession, menteeMember, menteeMember.getRole() == Member.Role.MENTOR ? com.back.domain.mentoring.session.entity.SenderRole.MENTOR : com.back.domain.mentoring.session.entity.SenderRole.MENTEE, content, messageType);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        ChatMessage result = chatMessageService.create(mentoringSession, menteeMember, content, messageType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getSender()).isEqualTo(menteeMember);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅 메시지를 저장하고 처리하여 응답 DTO를 반환한다.")
    void saveAndProcessMessage_returnsResponseDTO() {
        // given
        ChatMessageRequest request = new ChatMessageRequest(MessageType.TEXT, "테스트 메시지");
        ChatMessage chatMessage = ChatMessage.create(mentoringSession, menteeMember, menteeMember.getRole() == Member.Role.MENTOR ? com.back.domain.mentoring.session.entity.SenderRole.MENTOR : com.back.domain.mentoring.session.entity.SenderRole.MENTEE, request.content(), request.type());

        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        ChatMessageResponse response = chatMessageService.saveAndProcessMessage(menteeMember, mentoringSession, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.senderName()).isEqualTo(menteeMember.getNickname());
        assertThat(response.content()).isEqualTo(request.content());
        assertThat(response.createdAt()).isEqualTo(chatMessage.getTimestamp());
    }
}