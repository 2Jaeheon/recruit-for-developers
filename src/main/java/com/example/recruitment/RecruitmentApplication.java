package com.example.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling // 스케줄러 기능 활성화
public class RecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApplication.class, args);
        System.out.println("Recruitment Application Server is running...");
    }
/*
    @Bean
    CommandLineRunner run(ApplicationContext context) {
        return args -> {
            System.out.println("Starting JobPostingScheduler manualCrawl...");
            // JobPostingScheduler Bean을 가져와서 manualCrawl() 실행
            JobPostingScheduler scheduler = context.getBean(JobPostingScheduler.class);
            scheduler.scheduleJobPostingCrawl(); // 크롤링 실행

            System.out.println("JobPostingScheduler manualCrawl completed.");
        };
    }*/
}
