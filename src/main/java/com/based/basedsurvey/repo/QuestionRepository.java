package com.based.basedsurvey.repo;

import com.based.basedsurvey.data.Question;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface QuestionRepository extends CrudRepository<Question, Long> {
    Question findById(long id);
    Collection<Question> findAllBySurveyId(long id);

}