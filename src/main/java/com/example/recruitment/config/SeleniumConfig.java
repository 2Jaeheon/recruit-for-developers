package com.example.recruitment.config;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfig {

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36"
    };

    @Bean
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver",
            "src/main/resources/chromedriver/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=" + getRandomUserAgent());
        options.addArguments("--disable-blink-features=AutomationControlled"); // 자동화 감지 우회
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // options.addArguments("--headless"); // 필요 시 활성화 (Headless 모드)

        return new ChromeDriver(options);
    }

    private String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(USER_AGENTS);
        Random random = new Random();
        return userAgents.get(random.nextInt(userAgents.size()));
    }
}