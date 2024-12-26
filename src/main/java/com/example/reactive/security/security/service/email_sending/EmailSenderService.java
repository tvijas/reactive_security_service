package com.example.reactive.security.security.service.email_sending;

import com.example.reactive.security.exceptions.BasicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailAdmin;
    @Value("${frontend.url}")
    private String frontEndUrl;
    @Value("${server.base-url}")
    private String backendUrl;


    @Transactional
    public Mono<Void> sendSubmissionLink(String toEmail, String code, String base) {
        return Mono.fromCallable(() -> {
                    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
                    simpleMailMessage.setFrom(emailAdmin);
                    simpleMailMessage.setTo(toEmail);
                    simpleMailMessage.setSubject("Email verification");
                    String submissionUrl = frontEndUrl + "/user/" + base + "?code=" + code + "&email=" + toEmail;
                    simpleMailMessage.setText("To verify your action forward next link: \n\n" + submissionUrl);
                    try {
                        javaMailSender.send(simpleMailMessage);
                        return true;
                    } catch (MailException ex) {
                        log.error("Error occurred during email sending: " + ex.getMessage());
                        throw new BasicException(
                                Map.of("email", "Error occurred during email sending"),
                                HttpStatus.BAD_REQUEST
                        );
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
