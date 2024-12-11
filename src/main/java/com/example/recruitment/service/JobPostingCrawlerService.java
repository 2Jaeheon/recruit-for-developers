package com.example.recruitment.service;

import com.example.recruitment.model.entity.Company;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.repository.CompanyRepository;
import com.example.recruitment.repository.JobPostingRepository;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

@Service
@RequiredArgsConstructor
public class JobPostingCrawlerService {

    private final WebDriver driver;
    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;

    private static final String BASE_URL =
        "https://www.saramin.co.kr/zf_user/search?company_cd=0%2C1%2C2%2C3%2C4%2C5%2C6%2C7%2C9%2C10"
            + "&company_type=scale001%2Ckosdaq%2Cstock%2Ckospi%2Cscale004%2Cscale003%2Cscale005"
            + "&job_type=2%2C1&search_done=y&recruitPage=";

    private static final int MAX_PAGES = 3;
    private final Random random = new Random();

    public void crawlAndSaveJobPostings() {
        try {
            for (int page = 1; page <= MAX_PAGES; page++) {
                String url = BASE_URL + page + "&recruitSort=relation&recruitPageCount=40";
                System.out.println("크롤링 URL: " + url);

                driver.get(url);
                waitForPageLoad();

                List<WebElement> jobCards = driver.findElements(By.cssSelector("div.item_recruit"));
                for (int i = 0; i < jobCards.size(); i++) {
                    try {
                        // 매번 새롭게 jobCards 재조회
                        jobCards = driver.findElements(By.cssSelector("div.item_recruit"));
                        WebElement jobCard = jobCards.get(i);
                        processJobCard(jobCard);
                        waitRandomTime();
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
            quitDriver();
        }
    }

    private void processJobCard(WebElement jobCard) {
        try {
            String title = safeExtractText(jobCard, "h2.job_tit a");
            // job_sector 내 모든 a 태그의 텍스트 수집
            List<WebElement> jobSectorLinks = jobCard.findElements(
                By.cssSelector("div.job_sector a"));
            StringBuilder descriptionBuilder = new StringBuilder();
            for (WebElement link : jobSectorLinks) {
                descriptionBuilder.append(link.getText().trim()).append(", ");
            }
            String description = descriptionBuilder.toString().replaceAll(", $", ""); // 마지막 콤마 제거
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


    private void saveJobPosting(String companyName, String companyLink, String title,
        String location, String jobLink, String discription, String date) {
        try {
            Company company = crawlCompanyDetails(companyLink, companyName);
            Company finalCompany = company;
            company = companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(finalCompany));
            waitRandomTime();

            JobPosting jobPosting = crawlJobDetails(jobLink, company, title, location, discription,
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

    private JobPosting crawlJobDetails(String jobLink, Company company, String title,
        String location, String description, String date) {
        try {
            driver.get(jobLink);

            TimeUnit.SECONDS.sleep(1);

            waitForPageLoad();

            String employmentType = findValueByLabel("근무형태");
            String salary = findValueByLabel("급여");
            String education = findValueByLabel("학력");
            String experience = findValueByLabel("경력");

            JobPosting jobPosting = new JobPosting();
            jobPosting.setCompany(company);
            jobPosting.setTitle(title);
            jobPosting.setLocation(location);
            jobPosting.setDescription(description);
            jobPosting.setEmploymentType(employmentType);
            jobPosting.setSalary(salary);
            jobPosting.setEducation(education);
            jobPosting.setExperience(experience);
            jobPosting.setDeadline(date);
            jobPosting.setViewCount(0);

            driver.navigate().back(); // 메인 페이지로 돌아가기
            waitForPageLoad();
            return jobPosting;
        } catch (Exception e) {
            System.err.println("Error crawling job details: " + e.getMessage());
            return null;
        }
    }

    private String findValueByLabel(String label) {
        try {
            List<WebElement> dtElements = driver.findElements(By.cssSelector("dt"));
            for (WebElement dt : dtElements) {
                if (dt.getText().trim().contains(label)) {
                    // dt 옆의 dd 요소 가져오기
                    WebElement dd = dt.findElement(By.xpath("following-sibling::dd"));

                    // 일반적인 텍스트 반환
                    return dd.getText().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding value for label '" + label + "': " + e.getMessage());
        }
        return "";
    }

    private Company crawlCompanyDetails(String companyLink, String companyName)
        throws InterruptedException {
        driver.get(companyLink);
        TimeUnit.SECONDS.sleep(1);
        waitForPageLoad();

        String industry = "";
        String ceoName = "";
        String businessContent = "";
        String address = "";

        List<WebElement> detailGroups = driver.findElements(
            By.cssSelector("div.company_details_group"));
        for (WebElement group : detailGroups) {
            String title = safeExtractText(group, "dt.tit");
            String desc = safeExtractText(group, "dd.desc");

            if (title.contains("업종")) {
                industry = desc;
            } else if (title.contains("대표자명")) {
                ceoName = desc;
            } else if (title.contains("사업내용")) {
                businessContent = desc;
            } else if (title.contains("주소")) {
                address = safeExtractText(group, "dd.desc p"); // 주소는 p 태그 안에 존재
            }
        }

        Company company = new Company();
        company.setName(companyName);
        company.setIndustry(industry);
        company.setCeoName(ceoName);
        company.setBusinessContent(businessContent);
        company.setAddress(address);
        company.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        driver.navigate().back(); // 메인 페이지로 돌아가기
        waitForPageLoad();
        return company;
    }

    private String safeExtractText(WebElement parent, String selector) {
        try {
            WebElement element = parent.findElement(By.cssSelector(selector));
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));
    }

    private void waitRandomTime() {
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(2000) + 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver 종료 완료");
        }
    }
}