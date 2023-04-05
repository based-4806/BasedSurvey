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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = BasedSurveyApplication.class)
@AutoConfigureMockMvc
@Log
public class FillSurveyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    QuestionRepository qr;
    @Autowired
    SurveyRepository sr;
    @Test
    public void testInvalidSurveyIDFill() throws Exception{
        mockMvc.perform(get("/survey/9999/answer"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Survey does not exist")));
    }

    @Test
    public void testSurveyIsBeingEdited() throws Exception{
        // create survey and set status to being edited
        String name = "survey1";
        Survey s = new Survey(name);
        s.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        sr.save(s);

        mockMvc.perform(get("/survey/1/answer"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Survey is being edited")));
    }

    @Test
    public void testSurveyIsFinished() throws Exception{
        // create survey and set status to finished
        String name = "survey1";
        Survey s = new Survey(name);
        s.setStatus(Survey.SurveyStatuses.FINISHED);
        sr.save(s);

        mockMvc.perform(get("/survey/1/answer"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Survey is finished")));
    }


    @Test
    public void testSurveyHasNoQuestions() throws Exception{
        // create survey with no questions
        String name = "survey2";
        Survey s = new Survey(name);
        s.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(s);

        mockMvc.perform(get("/survey/1/answer"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Survey has no questions")));
    }

    @Test
    public void testFillSurvey() throws Exception {
        // create multiple choice question
        MultipleChoiceQuestion mq = new MultipleChoiceQuestion();
        String prompt1 = "Is this project super based or ultra based?:";
        mq.setPrompt(prompt1);
        String additionalInfo1 = "hint, it's ultra based";
        mq.setAdditionalInfo(additionalInfo1);
        String choice1 = "super based";
        String choice2 = "ultra based";

        List<String> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        mq.setOptions(choices);

        // create open answer question
        OpenAnswerQuestion oq = new OpenAnswerQuestion();
        String prompt2 = "How based is this project?";
        oq.setPrompt(prompt2);
        String additionalInfo2 = "hint, it's giga based";
        oq.setAdditionalInfo(additionalInfo2);

        // create range question
        RangeQuestion rq = new RangeQuestion();
        String prompt3 = "Rate how based this project is";
        rq.setPrompt(prompt3);
        String additionalInfo3 = "hint, select the max value";
        rq.setAdditionalInfo(additionalInfo3);
        rq.setLow(-10);
        rq.setHigh(10);

        // add questions to a survey
        String name = "survey3";
        Survey s = new Survey(name);
        s.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        List<Question> questions = new ArrayList<>();
        questions.add(mq);
        questions.add(oq);
        questions.add(rq);
        s.setQuestions(questions);
        sr.save(s);

        String param1 = "values" + mq.getId();
        String param2 = "values" + oq.getId();
        String param3 = "values" + rq.getId();

        // get the page to fill out the survey and expect it contains all the question prompts
        this.mockMvc.perform(get("/survey/1/answer")).andDo(print())
                .andExpect(content().string(containsString("Is this project super based or ultra based?:")))
                .andExpect(content().string(containsString("How based is this project?")))
                .andExpect(content().string(containsString("Rate how based this project is")))
                .andExpect(content().string(containsString("additional notes: hint, it's ultra based")))
                .andExpect(content().string(containsString("additional notes: hint, it's giga based")))
                .andExpect(content().string(containsString("additional notes: hint, select the max value")))
                .andExpect(status().isOk());

        // post 3 sets of responses to the survey
        this.mockMvc.perform(post("/survey/1/answer").param(param1,"super based").param(param2,"its really based").param(param3,"-5")).andExpect(status().isFound());
        this.mockMvc.perform(post("/survey/1/answer").param(param1,"super based").param(param2,"its giga based").param(param3,"9")).andExpect(status().isFound());
        this.mockMvc.perform(post("/survey/1/answer").param(param1,"ultra based").param(param2,"its mega based").param(param3,"10")).andExpect(status().isFound());

        // for each question check if the responses were actually added
        MultipleChoiceQuestion q1 = (MultipleChoiceQuestion) qr.findById(1);
        assertEquals(0, (int) q1.getResponses().get(0));
        assertEquals(0, (int) q1.getResponses().get(1));
        assertEquals(1, (int) q1.getResponses().get(2));

        OpenAnswerQuestion q2 = (OpenAnswerQuestion) qr.findById(2);
        assertEquals("its really based", q2.getResponses().get(0));
        assertEquals("its giga based", q2.getResponses().get(1));
        assertEquals("its mega based", q2.getResponses().get(2));

        RangeQuestion q3 = (RangeQuestion) qr.findById(3);
        assertEquals(-5.0f, q3.getResponses().get(0), 0.0);
        assertEquals(9.0f, q3.getResponses().get(1), 0.0);
        assertEquals(10.0f, q3.getResponses().get(2), 0.0);
    }

    @Test
    public void testFillEditingSurvey() throws Exception {
        // create multiple choice question
        MultipleChoiceQuestion mq = new MultipleChoiceQuestion();
        String prompt1 = "Is this project super based or ultra based?:";
        mq.setPrompt(prompt1);
        String additionalInfo1 = "hint, it's ultra based";
        mq.setAdditionalInfo(additionalInfo1);
        String choice1 = "super based";
        String choice2 = "ultra based";

        List<String> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        mq.setOptions(choices);

        // create open answer question
        OpenAnswerQuestion oq = new OpenAnswerQuestion();
        String prompt2 = "How based is this project?";
        oq.setPrompt(prompt2);
        String additionalInfo2 = "hint, it's giga based";
        oq.setAdditionalInfo(additionalInfo2);

        // create range question
        RangeQuestion rq = new RangeQuestion();
        String prompt3 = "Rate how based this project is";
        rq.setPrompt(prompt3);
        String additionalInfo3 = "hint, select the max value";
        rq.setAdditionalInfo(additionalInfo3);
        rq.setLow(-10);
        rq.setHigh(10);

        // add questions to a survey
        String name = "survey3";
        Survey s = new Survey(name);
        s.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        List<Question> questions = new ArrayList<>();
        questions.add(mq);
        questions.add(oq);
        questions.add(rq);
        s.setQuestions(questions);
        sr.save(s);

        String param1 = "values" + mq.getId();
        String param2 = "values" + oq.getId();
        String param3 = "values" + rq.getId();

        // post a response to the survey
        this.mockMvc.perform(post("/survey/1/answer")
                        .param(param1,"super based")
                        .param(param2,"its really based")
                        .param(param3,"-5"))
                .andExpect(content().string(containsString("Survey is being edited")))
                .andExpect(status().isOk());

        // for each question check if the responses were actually added
        MultipleChoiceQuestion q1 = (MultipleChoiceQuestion) qr.findById(1);
        assertTrue(q1.getResponses().isEmpty());

        OpenAnswerQuestion q2 = (OpenAnswerQuestion) qr.findById(2);
        assertTrue(q2.getResponses().isEmpty());

        RangeQuestion q3 = (RangeQuestion) qr.findById(3);
        assertTrue(q3.getResponses().isEmpty());
    }

    @Test
    public void testFillFinishedSurvey() throws Exception {
        // create multiple choice question
        MultipleChoiceQuestion mq = new MultipleChoiceQuestion();
        String prompt1 = "Is this project super based or ultra based?:";
        mq.setPrompt(prompt1);
        String additionalInfo1 = "hint, it's ultra based";
        mq.setAdditionalInfo(additionalInfo1);
        String choice1 = "super based";
        String choice2 = "ultra based";

        List<String> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        mq.setOptions(choices);

        // create open answer question
        OpenAnswerQuestion oq = new OpenAnswerQuestion();
        String prompt2 = "How based is this project?";
        oq.setPrompt(prompt2);
        String additionalInfo2 = "hint, it's giga based";
        oq.setAdditionalInfo(additionalInfo2);

        // create range question
        RangeQuestion rq = new RangeQuestion();
        String prompt3 = "Rate how based this project is";
        rq.setPrompt(prompt3);
        String additionalInfo3 = "hint, select the max value";
        rq.setAdditionalInfo(additionalInfo3);
        rq.setLow(-10);
        rq.setHigh(10);

        // add questions to a survey
        String name = "survey3";
        Survey s = new Survey(name);
        s.setStatus(Survey.SurveyStatuses.FINISHED);
        List<Question> questions = new ArrayList<>();
        questions.add(mq);
        questions.add(oq);
        questions.add(rq);
        s.setQuestions(questions);
        sr.save(s);

        String param1 = "values" + mq.getId();
        String param2 = "values" + oq.getId();
        String param3 = "values" + rq.getId();

        // post a response to the survey
        this.mockMvc.perform(post("/survey/1/answer")
                        .param(param1,"super based")
                        .param(param2,"its really based")
                        .param(param3,"-5"))
                .andExpect(content().string(containsString("Survey is finished")))
                .andExpect(status().isOk());

        // for each question check if the responses were actually added
        MultipleChoiceQuestion q1 = (MultipleChoiceQuestion) qr.findById(1);
        assertTrue(q1.getResponses().isEmpty());

        OpenAnswerQuestion q2 = (OpenAnswerQuestion) qr.findById(2);
        assertTrue(q2.getResponses().isEmpty());

        RangeQuestion q3 = (RangeQuestion) qr.findById(3);
        assertTrue(q3.getResponses().isEmpty());
    }
}