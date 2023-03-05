package com.based.basedsurvey;

import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.SurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BasedSurveyApplication {

    private SurveyRepository repo;

    @Autowired
    public BasedSurveyApplication(SurveyRepository repo){
        this.repo = repo;
    }

    public static void main(String[] args) {
        SpringApplication.run(BasedSurveyApplication.class, args);
    }

}
