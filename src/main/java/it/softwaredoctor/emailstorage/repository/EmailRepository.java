package it.softwaredoctor.emailstorage.repository;

import it.softwaredoctor.emailstorage.model.Email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
}
