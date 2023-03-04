package com.based.basedsurvey.repo;

import com.based.basedsurvey.data.Survey;
import org.springframework.data.repository.CrudRepository;

public interface SurveyRepository extends CrudRepository<Survey, Long> {
    Survey findSurveyById(long id);

}