package com.based.basedsurvey.controller;

import com.based.basedsurvey.BasedSurveyApplication;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.extern.java.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = BasedSurveyApplication.class)
@AutoConfigureMockMvc
@Log
public class ControllerHelperClassTest {
    @Autowired
    QuestionRepository qr;
    @Autowired
    SurveyRepository sr;
    @Test
    public void testGetSurvey(){

        // add My Survey 1 and My Survey 2 to survey repository
        Survey survey1 = new Survey("My Survey 1");
        sr.save(survey1);

        Survey survey2 = new Survey("My Survey 2");
        sr.save(survey2);

        // call getSurvey static method to fetch surveys
        assertEquals(ControllerHelperClass.getSurvey(1).getName(), "My Survey 1");
        assertEquals(ControllerHelperClass.getSurvey(2).getName(), "My Survey 2");

        // call getSurvey with invalid survey id and assert that an exception is thrown
        assertThrows(ResourceNotFoundException.class, () -> {
            ControllerHelperClass.getSurvey(3);
        });
    }

    @Test
    public void testGetQuestion() {

        // create and add 3 questions to question repository
        MultiplechoiceQuestion mcq = new MultiplechoiceQuestion();
        mcq.setPrompt("prompt 1");

        OpenAnswerQuestion oaq = new OpenAnswerQuestion();
        oaq.setPrompt("prompt 2");

        RangeQuestion rq = new RangeQuestion();
        rq.setPrompt("prompt 3");

        qr.save(mcq);
        qr.save(oaq);
        qr.save(rq);

        // call getQuestion static method to fetch questions
        assertEquals(ControllerHelperClass.getQuestion(1).getPrompt(), "prompt 1");
        assertEquals(ControllerHelperClass.getQuestion(2).getPrompt(), "prompt 2");
        assertEquals(ControllerHelperClass.getQuestion(3).getPrompt(), "prompt 3");

        // call getQuestion with invalid survey id and assert that an exception is thrown
        assertThrows(ResourceNotFoundException.class, () -> {
            ControllerHelperClass.getQuestion(4);
        });
    }

}