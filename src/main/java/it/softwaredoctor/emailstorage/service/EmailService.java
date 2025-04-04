package it.softwaredoctor.emailstorage.service;

import it.softwaredoctor.emailstorage.config.MailConfig;
import it.softwaredoctor.emailstorage.model.Attachment;
import it.softwaredoctor.emailstorage.model.Email;
import it.softwaredoctor.emailstorage.model.EmailStatus;
import it.softwaredoctor.emailstorage.repository.AttachmentRepository;
import it.softwaredoctor.emailstorage.repository.EmailRepository;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.SearchTerm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${custom.value}")
    private String customValue;

//    @Value("${custom.folder}")
//    private String customFolder;

    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;
    private final S3Service s3Service;
    private final AttachmentRepository attachmentRepository;
    private final MailConfig mailConfig;

    public void processComplete () {
        readAndStoreEmail();
    }

    private List<Email> findAllEmailByDate () {
        List<Email> emails = emailRepository.findAll();
        if (emails.isEmpty()) {
            return List.of();
        }
        return emails
                .stream()
                .filter(email -> compareDateEmail(email.getReceivedAt()))
                .toList();
    }

//    @Scheduled(cron = "0 0/3 * * * ?") // ogni 3 minuti
//    @Scheduled(cron = "0 0 23 * * ?") // Esegue ogni giorno alle 23:00
    private String generateReport() {
        List<Email> emails = findAllEmailByDate();
        if (emails.isEmpty()) {
            return "Report email storage\nNessuna email trovata oggi.";
        }
        long totWithAttachment = emails
                .stream()
                .filter(email -> email.getAttachments() != null
                        && !email.getAttachments().isEmpty()
                        && email.getStatus().equals(EmailStatus.ELABORATA))
                .count();
        long totWithoutAttachment = emails
                .stream()
                .filter(email -> (email.getAttachments() == null
                        || email.getAttachments().isEmpty())
                        && email.getStatus().equals(EmailStatus.LETTA))
                .count();
        long totEmail = emails
                .stream()
                .filter(email -> compareDateEmail(email.getReceivedAt()))
                .count();
        return "Report email storage\n" +
                "Totale email ricevute oggi: " + totEmail + "\n" +
                "Totale email con allegato: " + totWithAttachment + "\n" +
                "Totale email senza allegato: " + totWithoutAttachment;
    }

    private boolean compareDateEmail(LocalDate dateEmail) {
        Comparator<LocalDate> dateComparator = LocalDate::compareTo;
        int result = dateComparator.compare(LocalDate.now(), dateEmail);
        return result == 0;
    }

    public void sendReport() {
        try {
            String reportContent = generateReport();
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(username);
            helper.setTo(customValue);
            helper.setSubject("Daily Email Report");
            helper.setText(reportContent, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Errore nell'invio del report", e);
        }
    }

    private Message[] getMessages(Folder inbox) throws MessagingException {
        SearchTerm unseenSearchTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        Message[] messages = inbox.search(unseenSearchTerm);

        if (messages.length > 0) {
            // Ordinare i messaggi per data (più recente per primo)
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getReceivedDate().compareTo(m1.getReceivedDate());
                } catch (MessagingException e) {
                    return 0;
                }
            });
        }
        return messages;
    }

    private Email saveEmail() {
        Email email = new Email();
        email.setAttachments(new ArrayList<>());
       emailRepository.save(email);
        return email;
    }

    private void readAndStoreEmail() {
        Email storedEmail = saveEmail();
        List<Attachment> attachments = new ArrayList<>();
        try {
            Folder inbox = mailConfig.imapFolder();
            Message[] messages = getMessages(inbox);
            if (messages.length > 0) {
//                for (Message message : messages) {
                    // Estrazione del corpo e degli allegati dell'email
                    String body = "";
                    Message message1 = messages[0];
                    Object content = message1.getContent();
                    if (content instanceof MimeMultipart multipart) {
                        Pair<String, List<Attachment>> bodyAndAttachments = getBody(multipart);
                        body = bodyAndAttachments.getFirst();
                        attachments = bodyAndAttachments.getSecond();
                    }
                    createEmail(message1, body, attachments, storedEmail);
                    message1.setFlag(Flags.Flag.SEEN, true);
//                }
            } else {
               log.warn("Nessuna email non letta trovata.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Errore nella lettura e nel salvataggio delle email", e);
        }
    }

    private Pair<String, List<Attachment>> getBody(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        List<Attachment> attachments = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart part = mimeMultipart.getBodyPart(i);

            if (isAttachment(part)) {
                processAttachment(part, attachments);
            } else if (isText(part)) {
                appendTextContent(part, text);
            } else if (part.getContent() instanceof MimeMultipart innerMultipart) {
                Pair<String, List<Attachment>> innerBody = getBody(innerMultipart);
                appendTextContent(innerBody.getFirst(), text);
                attachments.addAll(innerBody.getSecond());
            }
        }

        return Pair.of(text.toString(), attachments);
    }

    private boolean isAttachment(BodyPart part) throws MessagingException {
        return Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition());
    }

    private boolean isText(BodyPart part) throws MessagingException {
        return part.isMimeType("text/plain") || part.isMimeType("text/html");
    }


    private void appendTextContent(BodyPart part, StringBuilder text) throws MessagingException, IOException {
        if (text.isEmpty()) {
            text.append(MimeUtility.decodeText(part.getContent().toString()));
        }
    }

    private void appendTextContent(String content, StringBuilder text) {
        if (text.isEmpty()) {
            text.append(content);
        }
    }

    private void processAttachment(BodyPart part, List<Attachment> attachments) throws MessagingException, IOException {
        String rawFileName = part.getFileName();
        String fileName = rawFileName != null ? MimeUtility.decodeText(rawFileName) : "unknown_attachment";

        try (InputStream attachmentStream = part.getInputStream()) {
            byte[] content = attachmentStream.readAllBytes();
            Attachment attachment = Attachment.builder()
                    .fileName(fileName)
                    .content(content)
                    .mimeType(part.getContentType())
                    .build();
            attachments.add(attachment);
        }
    }

    private void createEmail(Message message, String body, List<Attachment> attachments, Email email) throws MessagingException {
        String sender = getSender(message);
        LocalDate receivedDate = getReceivedDate(message);
        email.setSubject(message.getSubject());
        email.setSender(sender);
        email.setBody(body);
        email.setReceivedAt(receivedDate);
        if (attachments != null && !attachments.isEmpty()) {
            email.setStatus(EmailStatus.ELABORATA);
        } else {
            email.setStatus(EmailStatus.LETTA);
        }
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                String s3Path;
                try {
                    String safeFileName = attachment.getFileName().replaceAll("[\\\\/:*?\"<>|]", "_");
                    if (safeFileName.length() > 50) {
                        safeFileName = safeFileName.substring(0, 50);
                    }
                    File tempFile = File.createTempFile("attachment_", "_" + safeFileName);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(attachment.getContent());
                    }
                    s3Path = s3Service.uploadFile(tempFile);
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    throw new IllegalStateException("Error saving attachment to temp file", e);
                }
                attachment.setS3Path(s3Path);
                attachment.setMimeType(attachment.getMimeType());
                email.getAttachments().add(attachment);
                attachment.setEmailUuid(email.getUuidEmail());
                attachmentRepository.save(attachment);
            }
        }
        emailRepository.save(email);
    }

    private String getSender(Message message) {
        try {
            return  Optional.ofNullable(message.getFrom())
                    .filter(from -> from.length > 0)
                    .map(from -> from[0].toString())
                    .orElse("Unknown");
        } catch (MessagingException e) {
            throw new IllegalStateException("Error getting sender", e);
        }
    }

    private LocalDate getReceivedDate(Message message) {
        try {
            return Optional.ofNullable(message.getReceivedDate())
                    .map(date -> date.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate())
                    .orElse(LocalDate.now());
        } catch (MessagingException e) {
            throw new IllegalStateException("Error getting received date", e);
        }
    }
}