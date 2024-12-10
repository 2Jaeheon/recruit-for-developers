package com.example.recruitment.service;

import com.example.recruitment.model.entity.Company;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.repository.CompanyRepository;
import com.example.recruitment.repository.JobPostingRepository;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

/**
 * JobPostingCrawlerService
 * <p>
 * 채용 공고를 Saramin 웹사이트에서 크롤링하여 데이터베이스에 저장하는 서비스입니다. Selenium WebDriver를 활용해 페이지를 탐색하고 정보를 추출합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JobPostingCrawlerService {

    private final WebDriver driver; // Selenium WebDriver 인스턴스
    private final JobPostingRepository jobPostingRepository; // JobPosting 저장소
    private final CompanyRepository companyRepository; // Company 저장소

    // 크롤링할 기본 URL (사람인 사이트의 특정 필터 설정 페이지)
    private static final String BASE_URL =
        "https://www.saramin.co.kr/zf_user/search?company_cd=0%2C1%2C2%2C3%2C4%2C5%2C6%2C7%2C9%2C10"
            + "&company_type=scale001%2Ckosdaq%2Cstock%2Ckospi%2Cscale004%2Cscale003%2Cscale005"
            + "&job_type=2%2C1&search_done=y&recruitPage=";

    private static final int MAX_PAGES = 3; // 최대 페이지 수 (설정된 페이지까지만 크롤링)
    private final Random random = new Random(); // 랜덤 대기 시간을 위한 Random 객체

    /**
     * 크롤링 작업을 시작하고 데이터를 데이터베이스에 저장합니다. 각 페이지를 탐색하며 채용 공고를 처리합니다.
     */
    public void crawlAndSaveJobPostings() {
        try {
            for (int page = 1; page <= MAX_PAGES; page++) {
                String url = BASE_URL + page + "&recruitSort=relation&recruitPageCount=40";
                System.out.println("크롤링 URL: " + url);

                // 페이지 로딩
                driver.get(url);
                waitForPageLoad();

                // 각 공고 카드를 수집
                List<WebElement> jobCards = driver.findElements(By.cssSelector("div.item_recruit"));
                for (int i = 0; i < jobCards.size(); i++) {
                    try {
                        // 매번 새롭게 jobCards 재조회 (동적 요소를 처리하기 위함)
                        jobCards = driver.findElements(By.cssSelector("div.item_recruit"));
                        WebElement jobCard = jobCards.get(i);
                        processJobCard(jobCard); // 공고 카드 처리
                        waitRandomTime(); // 랜덤 대기 시간
                    } catch (Exception e) {
                        System.err.println(
                            "Error processing job card at index " + i + ": " + e.getMessage());
                    }
                }
                System.out.println(page + "페이지 크롤링 완료");
            }
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            quitDriver(); // 크롤링 종료 시 WebDriver 종료
        }
    }

    /**
     * 개별 채용 공고 카드를 처리합니다.
     *
     * @param jobCard 채용 공고 카드 요소
     */
    private void processJobCard(WebElement jobCard) {
        try {
            // 제목, 기술 스택 등 필드 수집
            String title = safeExtractText(jobCard, "h2.job_tit a");
            List<WebElement> jobSectorLinks = jobCard.findElements(
                By.cssSelector("div.job_sector a"));
            StringBuilder descriptionBuilder = new StringBuilder();
            for (WebElement link : jobSectorLinks) {
                descriptionBuilder.append(link.getText().trim()).append(", ");
            }
            String description = descriptionBuilder.toString().replaceAll(", $", "");
            String date = safeExtractText(jobCard, "div.job_date span.date");

            WebElement companyElement = jobCard.findElement(
                By.cssSelector("div.area_corp strong.corp_name a"));
            String companyName = companyElement.getText().trim();
            String companyLink = companyElement.getAttribute("href");
            String jobLink = jobCard.findElement(By.cssSelector("h2.job_tit a"))
                .getAttribute("href");
            String location = safeExtractText(jobCard, "div.job_condition span");

            saveJobPosting(companyName, companyLink, title, location, jobLink, description, date);

        } catch (Exception e) {
            System.err.println("Error processing job card: " + e.getMessage());
        }
    }

    /**
     * 채용 공고를 데이터베이스에 저장합니다.
     *
     * @param companyName 회사명
     * @param companyLink 회사 링크
     * @param title       공고 제목
     * @param location    근무 위치
     * @param jobLink     공고 상세 링크
     * @param description 기술 스택
     * @param date        마감일
     */
    private void saveJobPosting(String companyName, String companyLink, String title,
        String location, String jobLink, String description, String date) {
        try {
            Company company = crawlCompanyDetails(companyLink, companyName);
            company = companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(company));
            waitRandomTime();

            JobPosting jobPosting = crawlJobDetails(jobLink, company, title, location, description,
                date);
            if (jobPosting != null) {
                Optional<JobPosting> existingJob = jobPostingRepository.findByTitleAndCompany(title,
                    company);
                if (existingJob.isEmpty()) {
                    jobPostingRepository.save(jobPosting);
                    System.out.println("새 공고 저장: " + title + " | " + companyName);
                }
            }
            waitRandomTime();
        } catch (Exception e) {
            System.err.println("Error saving job posting: " + e.getMessage());
        }
    }

    /**
     * WebDriver 종료를 위한 메서드
     */
    @PreDestroy
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver 종료 완료");
        }
    }

    /**
     * 랜덤 대기 시간을 설정합니다.
     */
    private void waitRandomTime() {
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(2000) + 1000); // 1초 ~ 3초 랜덤 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 페이지 로딩을 기다립니다.
     */
    private void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));
    }

    /**
     * 주어진 부모 요소와 선택자에 따라 안전하게 텍스트를 추출합니다.
     *
     * @param parent   부모 요소
     * @param selector CSS 선택자
     * @return 추출된 텍스트
     */
    private String safeExtractText(WebElement parent, String selector) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
}