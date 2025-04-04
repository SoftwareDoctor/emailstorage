package it.softwaredoctor.emailstorage.service;

import it.softwaredoctor.emailstorage.repository.AttachmentRepository;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
}
