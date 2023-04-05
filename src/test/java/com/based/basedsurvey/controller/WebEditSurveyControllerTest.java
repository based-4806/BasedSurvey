package com.based.basedsurvey.controller;
import com.based.basedsurvey.BasedSurveyApplication;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.SneakyThrows;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = BasedSurveyApplication.class)
@AutoConfigureMockMvc
@Log
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class WebEditSurveyControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    QuestionRepository qr;
    @Autowired
    SurveyRepository sr;

    @SneakyThrows
    @Test
    public void testBeingFilled(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(survey);

        this.mockMvc.perform(get("/survey/1/edit")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(containsString("This survey cannot be edited as it is being filled or finished!"))
                );
    }

    @SneakyThrows
    @Test
    public void testBeingEdited(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        sr.save(survey);

        this.mockMvc.perform(get("/survey/1/edit")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(not(containsString("This survey cannot be edited as it is being filled or finished!")))
                );
    }

    @SneakyThrows
    @Test
    public void testFinished(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.FINISHED);
        sr.save(survey);

        this.mockMvc.perform(get("/survey/1/edit")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(containsString("This survey cannot be edited as it is being filled or finished!"))
                );
    }

    @SneakyThrows
    @Test
    public void testCreate(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        sr.save(survey);

        assertEquals(0, sr.findSurveyById(1).getQuestions().size());
        mockMvc.perform(post("/question/create").param("surveyID", "1").param("qt", "MULTIPLE_CHOICE").param("prompt", "Test").param("additionalInfo", "TestInfo")).andExpect(status().isFound());
        assertEquals(1, sr.findSurveyById(1).getQuestions().size());
    }

    @SneakyThrows
    @Test
    public void testEditQuestionOpen(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(survey);

        var question = new MultipleChoiceQuestion();
        question.setSurvey(survey);
        question.setPrompt("Test Question");
        qr.save(question);

        this.mockMvc.perform(get("/question/1")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(containsString("Associated survey is not open for editing"))
                );
    }

    @SneakyThrows
    @Test
    public void testEditQuestionClose(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        sr.save(survey);

        var question = new MultipleChoiceQuestion();
        question.setSurvey(survey);
        question.setPrompt("Test Question");
        qr.save(question);

        this.mockMvc.perform(get("/question/1")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(not(containsString("Associated survey is not open for editing")))
                );
    }

    @SneakyThrows
    @Test
    public void testUneditingCreate(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(survey);

        assertEquals(0, sr.findSurveyById(1).getQuestions().size());
        mockMvc.perform(post("/question/create").param("surveyID", "1").param("qt", "MULTIPLE_CHOICE").param("prompt", "Test").param("additionalInfo", "TestInfo"))
                .andExpect(content().string(containsString("Survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals(1, sr.findSurveyById(1).getQuestions().size());
    }
}