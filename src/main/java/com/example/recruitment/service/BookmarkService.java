package com.example.recruitment.service;

import com.example.recruitment.model.dto.BookmarkDTO;
import com.example.recruitment.model.entity.Bookmark;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.repository.BookmarkRepository;
import com.example.recruitment.repository.JobPostingRepository;
import com.example.recruitment.repository.UserRepository;
import com.example.recruitment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * BookmarkService
 * <p>
 * 이 서비스 클래스는 북마크 관련 비즈니스 로직을 처리합니다.
 * <ul>
 *     <li>북마크 추가/제거 (토글)</li>
 *     <li>북마크 목록 조회</li>
 *     <li>사용자 활동 로그 기록</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;       // 북마크 데이터 접근
    private final JobPostingRepository jobPostingRepository;   // 채용 공고 데이터 접근
    private final UserRepository userRepository;               // 사용자 데이터 접근
    private final JwtUtil jwtUtil;                             // JWT 유틸리티
    private final UserActivityLogService activityLogService;   // 사용자 활동 로그 서비스

    /**
     * 북마크 추가 또는 제거 (토글)
     * <p>
     * 주어진 채용 공고 ID에 대해 북마크가 존재하면 제거하고, 존재하지 않으면 추가합니다. 사용자 활동 로그도 기록됩니다.
     *
     * @param request      HTTP 요청 객체 (Authorization 헤더 포함)
     * @param jobPostingId 북마크할 채용 공고 ID
     * @return String       북마크 상태 메시지 ("추가" 또는 "제거")
     * @throws IllegalArgumentException 유효하지 않은 채용 공고 ID 또는 사용자일 경우 발생
     */
    public String toggleBookmark(HttpServletRequest request, Long jobPostingId) {
        // JWT에서 사용자 정보 추출
        User user = extractUserFromRequest(request);

        // 채용 공고 존재 여부 확인
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채용 공고입니다."));

        // 북마크 존재 여부 확인
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndJobPosting_Id(user,
            jobPostingId);

        if (existingBookmark.isPresent()) {
            // 북마크가 이미 존재하면 삭제
            bookmarkRepository.delete(existingBookmark.get());

            // 활동 로그 저장: 북마크 제거
            activityLogService.logActivity(request, "POST /bookmarks/" + jobPostingId,
                "채용 공고 '" + jobPosting.getTitle() + "'의 북마크를 제거했습니다.");

            return "북마크가 제거되었습니다.";
        } else {
            // 북마크가 존재하지 않으면 추가
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setJobPosting(jobPosting);
            bookmarkRepository.save(bookmark);

            // 활동 로그 저장: 북마크 추가
            activityLogService.logActivity(request, "POST /bookmarks/" + jobPostingId,
                "채용 공고 '" + jobPosting.getTitle() + "'를 북마크했습니다.");

            return "북마크가 추가되었습니다.";
        }
    }

    /**
     * 북마크 목록 조회
     * <p>
     * 현재 인증된 사용자의 북마크 목록을 조회합니다. 활동 로그를 기록하며, 결과를 페이지 단위로 반환합니다.
     *
     * @param request  HTTP 요청 객체 (Authorization 헤더 포함)
     * @param pageable 페이지네이션 및 정렬 정보
     * @return Page<BookmarkDTO> 북마크 목록 (DTO 형태)
     */
    public Page<BookmarkDTO> getBookmarks(HttpServletRequest request, Pageable pageable) {
        // JWT에서 사용자 정보 추출
        User user = extractUserFromRequest(request);

        // 사용자에 해당하는 북마크 목록 조회
        Page<Bookmark> bookmarks = bookmarkRepository.findByUser(user, pageable);

        // 활동 로그 저장: 북마크 목록 조회
        activityLogService.logActivity(request, "GET /bookmarks", "북마크 목록을 조회했습니다.");

        // 북마크 엔티티를 DTO 형태로 변환하여 반환
        return bookmarks.map(bookmark -> new BookmarkDTO(
            bookmark.getId(),                                // 북마크 ID
            bookmark.getJobPosting().getId(),               // 채용 공고 ID
            bookmark.getJobPosting().getTitle(),            // 채용 공고 제목
            bookmark.getCreatedAt().toString()              // 북마크 생성 날짜
        ));
    }

    /**
     * JWT 토큰에서 사용자 정보를 추출합니다.
     * <p>
     * Authorization 헤더에서 JWT를 추출하고, JWT에서 사용자 이메일을 확인하여 사용자 객체를 반환합니다.
     *
     * @param request HTTP 요청 객체 (Authorization 헤더 포함)
     * @return User   JWT에서 추출한 사용자 정보
     * @throws IllegalArgumentException 유효하지 않은 사용자일 경우 발생
     */
    private User extractUserFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7); // "Bearer " 부분 제거
        String email = jwtUtil.extractEmail(token); // JWT에서 이메일 추출
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }
}