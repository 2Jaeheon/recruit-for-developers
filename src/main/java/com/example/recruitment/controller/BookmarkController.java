package com.example.recruitment.controller;

import com.example.recruitment.model.dto.BookmarkDTO;
import com.example.recruitment.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookmark", description = "북마크 관리 API") // Swagger 문서용 태그 추가
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 채용 공고에 대한 북마크를 토글하는 API
     *
     * @param request      HTTP 요청 객체 (인증 정보 포함)
     * @param jobPostingId 북마크할 채용 공고 ID
     * @return 북마크 상태 메시지 ("북마크 추가됨" 또는 "북마크 제거됨")
     */
    @Operation(summary = "북마크 토글", description = "채용 공고에 대한 북마크를 추가하거나 제거합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "북마크 상태 메시지 반환")
    })
    @PostMapping("/{jobPostingId}")
    public ResponseEntity<String> toggleBookmark(
        @Parameter(description = "북마크할 채용 공고 ID", required = true) @PathVariable Long jobPostingId,
        HttpServletRequest request) {
        // 북마크 서비스 호출 및 상태 메시지 반환
        String message = bookmarkService.toggleBookmark(request, jobPostingId);
        return ResponseEntity.ok(message);
    }

    /**
     * 북마크 목록 조회 API
     *
     * @param page      페이지 번호 (기본값: 0)
     * @param size      페이지 크기 (기본값: 20)
     * @param sortBy    정렬 기준 필드 (기본값: createdAt)
     * @param direction 정렬 방향 (asc 또는 desc, 기본값: desc)
     * @return 북마크된 채용 공고 목록 (페이징 적용)
     */
    @Operation(summary = "북마크 목록 조회", description = "북마크된 채용 공고 목록을 조회합니다. 페이징 및 정렬 기능을 제공합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<BookmarkDTO>> getBookmarks(
        @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기 (기본값: 20)") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "정렬 기준 필드 (기본값: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "정렬 방향 (asc 또는 desc, 기본값: desc)") @RequestParam(defaultValue = "desc") String direction,
        HttpServletRequest request) {

        // 페이징 및 정렬 설정
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(direction), sortBy));

        // 북마크 서비스 호출 및 결과 반환
        Page<BookmarkDTO> bookmarks = bookmarkService.getBookmarks(request, pageable);
        return ResponseEntity.ok(bookmarks);
    }
}