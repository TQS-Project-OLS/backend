package com.example.OLSHEETS;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
    "spring.main.lazy-initialization=true"
})
public class CucumberSpringConfiguration {
}
