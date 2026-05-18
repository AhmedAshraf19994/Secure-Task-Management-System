package com.ahmed.Secure.Task.Management.System.emailService;

import com.ahmed.Secure.Task.Management.System.emailService.emailTemplates.EmailTemplate;
import com.ahmed.Secure.Task.Management.System.system.exceptions.CustomEmailSendingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String from;

    public void sendHtmlEmail(EmailTemplate emailTemplate)  {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,"utf-8");
            helper.setFrom("no-reply@task-management.com");
            helper.setTo(emailTemplate.to());
            helper.setSubject(emailTemplate.subject());

            Context context = new Context();
            context.setVariables(emailTemplate.templateVariables());
            String htmlContent = templateEngine.process(emailTemplate.templateName(), context);

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new CustomEmailSendingException("Failed to send email  ", e);
        }

    }
}
