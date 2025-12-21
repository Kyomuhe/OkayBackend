package com.ceres.project.utils.mail;

import com.alibaba.fastjson2.JSONObject;
import com.ceres.project.models.database.SystemUserModel;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.UnsupportedEncodingException;


@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    public void sendTemplateMail(SystemUserModel receiver, String subject, String mailBody, String template) throws MessagingException, UnsupportedEncodingException {
        Context context = new Context();

        context.setVariable("logo", "images/sclogo.png");
        context.setVariable("body", mailBody);

        String process = templateEngine.process(template, context);
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setSubject(subject);
        helper.setFrom("servspace@servicecops.com", "Servspace Support");
        helper.setText(process, true);
        helper.setTo(receiver.getEmail());
        emailSender.send(mimeMessage);
    }

    public void sendTemplateMailToEmail(String receiverEmail, JSONObject variables, String subject, String template) throws MessagingException, UnsupportedEncodingException {
        Context context = new Context();

        var keys = variables.keySet();
        keys.forEach(k -> context.setVariable(k, variables.get(k)));

        String process = templateEngine.process(template, context);
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setSubject(subject);
        helper.setFrom("servspace@servicecops.com", "Servspace Support");
        helper.setText(process, true);
        helper.setTo(receiverEmail);
        emailSender.send(mimeMessage);
    }

    private MimeMultipart createMultipartContent(String htmlContent, String imagePath) throws MessagingException {
        MimeMultipart multipart = new MimeMultipart("related");

        // Create the HTML body part
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

        // Create the image body part
        MimeBodyPart imagePart = new MimeBodyPart();
        File imageFile = new File(imagePath);
        DataSource dataSource = new FileDataSource(imageFile);
        imagePart.setDataHandler(new DataHandler(dataSource));
        imagePart.setHeader("Content-ID", "<illustration>"); // Set content ID for reference in HTML

        // Add parts to the multipart message
        multipart.addBodyPart(htmlPart);
        multipart.addBodyPart(imagePart);

        return multipart;
    }

    public void sendTestMail() {
        SystemUserModel receiver = new SystemUserModel();
        receiver.setFirstName("Azandu");
        receiver.setLastName("Man");
        receiver.setEmail("xandu1738@gmail.com");

        try {
            String mailBody = """
                    <p>
                                Dear %s<br/>
                                Welcome to Servspace! We're thrilled to have you join our community of talented freelancers.<br/>
                                To help you get the most out of your experience, we recommend starting with these two important steps:<br/>
                            </p>
                            <p>
                                Add Your Skills:<br/>
                                Head over to your profile and list your skills. This helps us understand your expertise and find you for
                                relevant projects.
                            </p>
                            <p>
                                Attempt Assessments:<br/>
                                Showcase your proficiency by taking skill assessments. This not only boosts your profile visibility but also
                                increases your chances of landing more projects.
                            </p>
                            <p>
                                If you have any questions or need assistance, our support team is here to help. Feel free to raise an issue
                                by clicking the chat feature on your home screen.<br/>
                                Once again, welcome aboard! We can't wait to see the amazing work you'll do.
                    
                                <a th:href="servspace.servicecops.com">Get Started</a>
                            </p>
                    """;

            log.info("Sending test mail");
            sendTemplateMail(receiver, "Welcome aboard Dude", String.format(mailBody, receiver.getFirstName().concat(" ").concat(receiver.getLastName())), "email-template");
            log.info("Test mail sent");
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
