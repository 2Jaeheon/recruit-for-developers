package com.example.recruitment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Recruitment API 문서")
                .version("1.0.0")
                .description("API 문서를 통해 채용 시스템의 기능을 확인할 수 있습니다.\n\n" +
                    "### 🔑 JWT 인증 사용 방법:\n" +
                    "- **Authorize 버튼**을 클릭한 후, 토큰 값만 입력하세요.\n" +
                    "- **'Bearer '**는 입력하지 않아도 됩니다. 시스템에서 자동으로 추가됩니다.\n\n" +
                    "예시 입력:\n" +
                    "```\n" +
                    "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29t...\n" +
                    "```\n\n"
                    + "### API 예외 처리 및 메시지에 대한 안내\n"
                    + "CustomException과 GlobalExceptionHandler 를 사용하여 예외처리를 구현하려고 하였으나\n"
                    + "예외처리를 하면 Swagger 및 api-docs가 나타나지 않아 따로 예외 메시지를 출력할 수 없었습니다."))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}