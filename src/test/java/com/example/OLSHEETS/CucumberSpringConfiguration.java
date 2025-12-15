package com.example.OLSHEETS;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
    "spring.main.lazy-initialization=true"
})
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
