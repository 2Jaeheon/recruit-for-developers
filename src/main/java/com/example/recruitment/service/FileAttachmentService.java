package com.example.recruitment.service;

import com.example.recruitment.model.entity.Application;
import com.example.recruitment.model.entity.FileAttachment;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.repository.FileAttachmentRepository;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * FileAttachmentService
 * <p>
 * 이 서비스 클래스는 파일 첨부 정보를 데이터베이스에 저장하는 비즈니스 로직을 담당합니다. 주로 이력서 파일과 관련된 작업을 처리하며, 파일 경로와 파일명을 저장합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository; // 파일 첨부 정보 저장소

    /**
     * 파일 첨부 정보를 저장합니다.
     * <p>
     * 주어진 이력서 경로(resumePath)를 사용하여 파일명을 추출한 뒤, 사용자와 지원 정보를 연결해 저장합니다.
     * </p>
     *
     * @param user        파일을 업로드한 사용자
     * @param application 관련된 지원 정보 (Application 엔티티)
     * @param resumePath  파일 경로 (저장된 이력서 경로)
     */
    public void saveFileAttachment(User user, Application application, String resumePath) {
        // 새로운 FileAttachment 엔티티 객체 생성
        FileAttachment fileAttachment = new FileAttachment();

        // 사용자, 지원 정보 설정
        fileAttachment.setUser(user);
        fileAttachment.setApplication(application);
        fileAttachment.setFilePath(resumePath);

        // 파일 경로에서 파일명 추출 (파일명만 저장)
        Path path = Paths.get(resumePath);       // 파일 경로 객체 생성
        String fileName = path.getFileName().toString(); // 파일명 추출
        fileAttachment.setFileName(fileName);    // 파일명 설정

        // 파일 첨부 정보 저장
        fileAttachmentRepository.save(fileAttachment);
    }
}