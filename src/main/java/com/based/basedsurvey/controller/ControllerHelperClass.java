package com.based.basedsurvey.controller;
import com.based.basedsurvey.data.Question;
import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
public class ControllerHelperClass {
    private static QuestionRepository questionRepository;
    private static SurveyRepository surveyRepository;

    @Autowired
    private ControllerHelperClass(QuestionRepository qr, SurveyRepository sr){
        questionRepository = qr;
        surveyRepository = sr;
    }

    /**
     * Uses ORM to find a survey. If it doesn't exist, an exception is thrown
     * @param id the id of the survey
     * @return the survey
     */
    public static Survey getSurvey(long id){
        var survey = surveyRepository.findSurveyById(id);
        if(survey == null){
            log.warning("Survey with ID: "+id+" does not exist");
            throw new ResourceNotFoundException();
        }
        return survey;
    }

    /**
     * Uses ORM to find a question. If it doesn't exist, an exception is thrown
     * @param id the id of the question
     * @return the question
     */
    public static Question getQuestion(long id){
        var question = questionRepository.findById(id);
        if(question == null){
            log.warning("Question with ID: "+id+" does not exist");
            throw new ResourceNotFoundException();
        }
        return question;
    }
}
