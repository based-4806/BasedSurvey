package com.based.basedsurvey.controller;

import com.based.basedsurvey.BasedSurveyApplication;
import com.based.basedsurvey.data.MultiplechoiceQuestion;
import com.based.basedsurvey.data.OpenAnswerQuestion;
import com.based.basedsurvey.data.Question;
import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class WebQuestionControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    QuestionRepository qr;
    @Autowired
    SurveyRepository sr;

    @Before
    public void setup(){
        var survey = new Survey();
        survey.setName("Test Survey");
        sr.save(survey);


        {
            var question = new MultiplechoiceQuestion();
            question.setSurvey(survey);
            question.setPrompt("Test Question");
            qr.save(question);
        }

        {
            var question = new OpenAnswerQuestion();
            question.setSurvey(survey);
            question.setPrompt("Open Answer Test Question");
            qr.save(question);
        }

    }
    @SneakyThrows
    @Test
    public void testMcq(){
        this.mockMvc.perform(get("/question/1")).andDo(print()).andExpect(status().isOk());

        // rename
        assertEquals("Test Question",qr.findById(1).getPrompt()); //control
        this.mockMvc.perform(post("/question/rename").param("id","1").param("prompt","Changed")).andExpect(status().isFound());
        assertEquals("Changed",qr.findById(1).getPrompt());

        // add option
        assertEquals(0,((MultiplechoiceQuestion)qr.findById(1)).getOptions().size()); //control
        this.mockMvc.perform(post("/question/addOption").param("id","1").param("option","DummyOption")).andExpect(status().isFound());
        assertEquals(1,((MultiplechoiceQuestion)qr.findById(1)).getOptions().size());
        assertEquals("DummyOption",((MultiplechoiceQuestion)qr.findById(1)).getOptions().get(0));

        //remove option
        //control above
        this.mockMvc.perform(post("/question/removeOption").param("id","1").param("optionIndex","0")).andExpect(status().isFound());
        assertEquals(0,((MultiplechoiceQuestion)qr.findById(1)).getOptions().size());
        log.info("Final result");
        this.mockMvc.perform(get("/question/1")).andDo(print()).andExpect(status().isOk());

    }

    @SneakyThrows
    @Test
    public void testOAQ() {
        this.mockMvc.perform(get("/question/2")).andDo(print()).andExpect(
                status().isOk()).andExpect(
                        content().string(not(containsString("Question type not implemented"))))
                .andExpect(
                        content().string(containsString("Nothing else is needed for this type of question"))
                );


        // rename
        assertEquals("Open Answer Test Question", qr.findById(2).getPrompt()); //control
        this.mockMvc.perform(post("/question/rename").param("id", "2").param("prompt", "Changed")).andExpect(status().isFound());
        assertEquals("Changed", qr.findById(2).getPrompt());
    }
}