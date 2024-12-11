package com.example.recruitment.service;

import com.example.recruitment.model.entity.Application;
import com.example.recruitment.model.entity.FileAttachment;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.repository.FileAttachmentRepository;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;

    // 파일 정보 저장
    public void saveFileAttachment(User user, Application application, String resumePath) {
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setUser(user);
        fileAttachment.setApplication(application);
        fileAttachment.setFilePath(resumePath);

        // 파일 이름 추출
        Path path = Paths.get(resumePath);
        String fileName = path.getFileName().toString();
        fileAttachment.setFileName(fileName);

        fileAttachmentRepository.save(fileAttachment);
    }
}