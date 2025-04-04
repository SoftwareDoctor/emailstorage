package it.softwaredoctor.emailstorage.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid_email", unique = true)
    private UUID uuidEmail;

    @Column(name = "subject", length = 2000000)
    private String subject;

    private String sender;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    private LocalDate receivedAt;

    @Column(name = "body", length = 2000000)
    private String body;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Attachment> attachments;

    @PrePersist
    private void onCreate() {
        if (this.uuidEmail == null)
            this.uuidEmail = UUID.randomUUID();
    }
}
