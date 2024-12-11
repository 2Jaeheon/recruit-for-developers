package com.example.recruitment.repository;

import com.example.recruitment.model.entity.Bookmark;
import com.example.recruitment.model.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 사용자와 공고 ID를 기준으로 북마크 조회 (토글 처리용)
    Optional<Bookmark> findByUserAndJobPosting_Id(User user, Long jobPostingId);

    // 사용자별 북마크 조회 (페이지네이션 지원)
    Page<Bookmark> findByUser(User user, Pageable pageable);
}