package it.softwaredoctor.emailstorage.config;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.mail.yahoo.com");
        mailSender.setProtocol("smtp");
        mailSender.setPort(587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.smtp.debug", "true");
        return mailSender;
    }

    @Bean
    public Session imapSession() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.mail.yahoo.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "false");
        props.put("mail.imaps.starttls.enable", "true");
        props.put("mail.imaps.auth", "true");
        return Session.getInstance(props);
    }

    @Bean
    public Folder imapFolder() throws MessagingException {
        Store store;
        Folder inbox;
        Session session = imapSession();
        store = session.getStore("imaps");
        store.connect("imap.mail.yahoo.com", username, password);
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        return inbox;
    }
}
