package it.softwaredoctor.emailstorage.repository;


import it.softwaredoctor.emailstorage.model.Attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
