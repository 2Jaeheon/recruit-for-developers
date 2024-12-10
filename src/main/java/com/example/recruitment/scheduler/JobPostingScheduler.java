package com.example.recruitment.scheduler;

import com.example.recruitment.service.JobPostingCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 채용 공고 크롤링을 주기적으로 실행하는 스케줄러 클래스
 */
@Component
@RequiredArgsConstructor
public class JobPostingScheduler {

    private final JobPostingCrawlerService jobPostingCrawlerService;

    /**
     * 매일 자정(00:00)에 채용 공고를 크롤링하고 DB에 저장하는 작업 실행
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void scheduleJobPostingCrawl() {
        System.out.println("스케줄링 시작: 채용 공고 크롤링 실행");
        jobPostingCrawlerService.crawlAndSaveJobPostings();
        System.out.println("스케줄링 완료: 채용 공고 크롤링 실행 완료");
    }

    /**
     * 테스트용: 매 5분마다 실행
     * - 개발 및 테스트 시 사용 가능
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행 (300,000ms)
    public void testCrawlJobPostings() {
        System.out.println("테스트 스케줄링 시작: 채용 공고 크롤링 실행");
        jobPostingCrawlerService.crawlAndSaveJobPostings();
        System.out.println("테스트 스케줄링 완료: 채용 공고 크롤링 실행 완료");
    }
}