package it.softwaredoctor.emailstorage.controller;

import it.softwaredoctor.emailstorage.service.EmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/")
    public ResponseEntity<Void> processComplete() {
        emailService.processComplete();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/report")
    public ResponseEntity<Void> processReport() {
        emailService.sendReport();
        return ResponseEntity.ok().build();
    }
}
