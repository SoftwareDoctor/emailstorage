package it.softwaredoctor.emailstorage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid_attachment", unique = true)
    private UUID uuidAttachment;

    private UUID emailUuid;

    private String fileName;

    private String s3Path;

    private String mimeType;

    @Lob
    private byte[] content;

    @PrePersist
    private void onCreate() {
        if (this.uuidAttachment == null)
            this.uuidAttachment = UUID.randomUUID();
    }
}
