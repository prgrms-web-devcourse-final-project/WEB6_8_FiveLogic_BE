package com.back.global.mail;

import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class MailDebugController {

    private final ApplicationContext context;

    public MailDebugController(ApplicationContext context) {
        this.context = context;
    }

    @GetMapping("/debug/mail")
    public String debugMail() {
        StringBuilder sb = new StringBuilder();

        // MailConfig 빈 확인
        sb.append("MailConfig bean exists: ").append(context.containsBean("mailConfig")).append("\n");

        // JavaMailSender 빈 확인
        sb.append("JavaMailSender bean exists: ").append(context.containsBean("javaMailSender")).append("\n");

        // 모든 Configuration 빈 확인
        sb.append("\nAll Configuration beans:\n");
        Arrays.stream(context.getBeanDefinitionNames())
            .filter(name -> name.toLowerCase().contains("config"))
            .forEach(name -> sb.append("- ").append(name).append("\n"));

        // JavaMailSender 타입의 빈 확인
        try {
            JavaMailSender mailSender = context.getBean(JavaMailSender.class);
            sb.append("\nJavaMailSender found: ").append(mailSender.getClass().getName());
        } catch (Exception e) {
            sb.append("\nJavaMailSender NOT found: ").append(e.getMessage());
        }

        return sb.toString();
    }
}
