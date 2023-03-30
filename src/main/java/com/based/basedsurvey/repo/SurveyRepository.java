package com.based.basedsurvey.repo;

import com.based.basedsurvey.data.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Survey findSurveyById(long id);
}