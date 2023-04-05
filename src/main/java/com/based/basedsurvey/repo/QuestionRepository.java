package com.based.basedsurvey.repo;

import com.based.basedsurvey.data.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Question findById(long id);
    Collection<Question> findAllBySurveyId(long id);

    Page<Question> findAllBySurveyId(long id, Pageable pageable);


}