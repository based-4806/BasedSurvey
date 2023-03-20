package com.based.basedsurvey.data;

import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class SurveyTest {

    @Autowired
    QuestionRepository qr;
    @Autowired
    SurveyRepository sr;

    private Survey survey;

    @Before
    public void setup(){
        sr.deleteAll();
        Survey survey = new Survey("My Survey");
        sr.save(survey);
        this.survey = sr.findSurveyById(survey.getId());

    }

    @Test
    public void testAll(){
        //test attributes
        assertEquals("My Survey", survey.getName());
        assertEquals(0, survey.getQuestions().size());
        assertTrue(survey.isOpen());
        //add questions to survey
        survey.getQuestions().add(openQuestion());
        survey.getQuestions().add(multiQuestion());
        survey.getQuestions().add(rangeQuestion());
        sr.save(survey);
        assertEquals(3,qr.count());

        // get questions through survey
        var q1 = sr.findSurveyById(survey.getId()).getQuestions().get(0);
        var q2 = sr.findSurveyById(survey.getId()).getQuestions().get(1);
        var q3 = sr.findSurveyById(survey.getId()).getQuestions().get(2);

        assertEquals("Question 1 prompt", q1.getPrompt());
        assertEquals("Question 2 prompt", q2.getPrompt());
        assertEquals("Question 3 prompt", q3.getPrompt());

        // get survey through question
        assertEquals(survey, q1.getSurvey());

        //update question
        var multichoice = (MultiplechoiceQuestion) q2;
        multichoice.getResponses().add(10);
        multichoice.getResponses().add(9);
        multichoice.getResponses().add(8);
        qr.save(multichoice);

        assertEquals(3,((MultiplechoiceQuestion)qr.findById(multichoice.getId())).getResponses().size());

        //find all by survey id
        assertEquals(3, qr.findAllBySurveyId(survey.getId()).size());
        //close survey
        survey.setOpen(false);
        sr.save(survey);
        assertFalse(sr.findAll().iterator().next().isOpen());

        // remove questions from survey
        survey.getQuestions().remove(0);
        sr.save(survey);
        assertEquals(2, qr.count());
    }


    private OpenAnswerQuestion openQuestion(){
        OpenAnswerQuestion q = new OpenAnswerQuestion();
        q.setSurvey(survey);
        q.setPrompt("Question 1 prompt");
        return q;
    }
    private MultiplechoiceQuestion multiQuestion(){
        MultiplechoiceQuestion q = new MultiplechoiceQuestion();
        q.setSurvey(survey);
        q.setPrompt("Question 2 prompt");
        q.getOptions().add("Option 1");
        q.getOptions().add("Option 2");
        return q;
    }

    private RangeQuestion rangeQuestion(){
        RangeQuestion q = new RangeQuestion();
        q.setSurvey(survey);
        q.setPrompt("Question 3 prompt");
        q.setLow(0);
        q.setHigh(10);
        return q;
    }
}