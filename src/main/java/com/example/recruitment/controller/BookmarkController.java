package com.example.recruitment.controller;

import com.example.recruitment.model.dto.BookmarkDTO;
import com.example.recruitment.service.BookmarkService;
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

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{jobPostingId}")
    public ResponseEntity<String> toggleBookmark(HttpServletRequest request,
        @PathVariable Long jobPostingId) {
        String message = bookmarkService.toggleBookmark(request, jobPostingId);
        return ResponseEntity.ok(message); // 북마크 상태 메시지 반환
    }

    /**
     * 북마크 목록 조회 API
     *
     * @param page      페이지 번호
     * @param size      페이지 크기
     * @param sortBy    정렬 기준
     * @param direction 정렬 방향
     * @return 북마크 목록
     */
    @GetMapping
    public ResponseEntity<Page<BookmarkDTO>> getBookmarks(HttpServletRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<BookmarkDTO> bookmarks = bookmarkService.getBookmarks(request, pageable);
        return ResponseEntity.ok(bookmarks);
    }
}