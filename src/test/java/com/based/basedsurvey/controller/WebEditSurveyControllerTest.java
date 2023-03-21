package com.based.basedsurvey.controller;
import com.based.basedsurvey.BasedSurveyApplication;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

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
    public void testOpen(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setOpen(true);
        sr.save(survey);

        this.mockMvc.perform(get("/survey/1/edit")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(containsString("This survey cannot be edited as it is open!"))
                );
    }

    @SneakyThrows
    @Test
    public void testClose(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setOpen(false);
        sr.save(survey);

        this.mockMvc.perform(get("/survey/1/edit")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(not(containsString("This survey cannot be edited as it is open!")))
                );
    }

    @SneakyThrows
    @Test
    public void testCreate(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setOpen(false);
        sr.save(survey);

        assertEquals(0, sr.findSurveyById(1).getQuestions().size());
        mockMvc.perform(post("/question/create").param("surveyID", "1").param("qt", "MULTIPLE_CHOICE").param("prompt", "Test")).andExpect(status().isFound());
        assertEquals(1, sr.findSurveyById(1).getQuestions().size());
    }

    @SneakyThrows
    @Test
    public void testEditQuestionOpen(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setOpen(true);
        sr.save(survey);

        var question = new MultiplechoiceQuestion();
        question.setSurvey(survey);
        question.setPrompt("Test Question");
        qr.save(question);

        this.mockMvc.perform(get("/question/1")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(containsString("This survey cannot be edited as it is open!"))
                );
    }

    @SneakyThrows
    @Test
    public void testEditQuestionClose(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setOpen(false);
        sr.save(survey);

        var question = new MultiplechoiceQuestion();
        question.setSurvey(survey);
        question.setPrompt("Test Question");
        qr.save(question);

        this.mockMvc.perform(get("/question/1")).andDo(print()).andExpect(
                        status().isOk())
                .andExpect(
                        content().string(not(containsString("This survey cannot be edited as it is open!")))
                );
    }
}