package com.back.domain.member.member.email;

import com.back.global.exception.ServiceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
        if (mailSender == null) {
            log.warn("JavaMailSender is not available. Email sending will be disabled.");
        }
    }

    /**
     * 간단한 텍스트 이메일 발송
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not available. Skipping email to: {}", to);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("snake20011600@gmail.com", "JobMate");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false); // false = plain text

            mailSender.send(message);
            log.info("텍스트 이메일 발송 성공: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("텍스트 이메일 발송 실패: {}", to, e);
            throw new ServiceException("500-1", "텍스트 이메일 발송에 실패했습니다.");
        }
    }

    /**
     * HTML 이메일 발송
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not available. Skipping email to: {}", to);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("snake20011600@gmail.com", "JobMate");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.info("HTML 이메일 발송 성공: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("HTML 이메일 발송 실패: {}", to, e);
            throw new ServiceException("500-2", "HTML 이메일 발송에 실패했습니다.");
        }
    }

    /**
     * 인증번호 이메일 발송 (멘토 회원가입용)
     */
    public void sendVerificationCode(String to, String verificationCode) {
        String subject = "[FiveLogic] 멘토 회원가입 인증번호";
        String htmlContent = buildVerificationEmailHtml(verificationCode);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 인증번호 이메일 HTML 템플릿
     */
    private String buildVerificationEmailHtml(String verificationCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; background-color: #f9f9f9; }
                        .code-box { background-color: #fff; border: 2px solid #4CAF50; padding: 20px; text-align: center; margin: 20px 0; }
                        .code { font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 5px; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>JobMate 멘토 회원가입</h1>
                        </div>
                        <div class="content">
                            <h2>인증번호 확인</h2>
                            <p>안녕하세요. FiveLogic입니다.</p>
                            <p>멘토 회원가입을 위한 인증번호를 안내드립니다.</p>
                            <p>아래 인증번호를 입력하여 회원가입을 완료해주세요.</p>

                            <div class="code-box">
                                <div class="code">%s</div>
                            </div>

                            <p><strong>※ 인증번호는 5분간 유효합니다.</strong></p>
                            <p>본인이 요청하지 않은 경우, 이 메일을 무시하셔도 됩니다.</p>
                        </div>
                        <div class="footer">
                            <p>© 2025 FiveLogic. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationCode);
    }
}
