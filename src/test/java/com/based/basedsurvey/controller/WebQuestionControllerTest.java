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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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

    private static long q1ID;
    private static long q2ID;
    private static long q3ID;

    @Before
    public void setup(){
        var survey = new Survey();
        survey.setName("Test Survey");
        survey.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        sr.save(survey);


        {
            var question = new MultipleChoiceQuestion();
            question.setSurvey(survey);
            question.setPrompt("Test Question");
            question.setAdditionalInfo("this project is based");
            qr.save(question);
            q1ID = question.getId();
        }

        {
            var question = new OpenAnswerQuestion();
            question.setSurvey(survey);
            question.setPrompt("Open Answer Test Question");
            question.setAdditionalInfo("this project is based");
            qr.save(question);
            q2ID = question.getId();
        }

        {
            var question = new RangeQuestion();
            question.setSurvey(survey);
            question.setPrompt("Range Test Question");
            question.setAdditionalInfo("this project is based");
            qr.save(question);
            q3ID = question.getId();
        }

    }
    @SneakyThrows
    @Test
    public void testMcq(){
        this.mockMvc.perform(get("/question/" + q1ID)).andDo(print()).andExpect(status().isOk());

        // rename
        assertEquals("Test Question",qr.findById(q1ID).getPrompt()); //control
        assertEquals("this project is based",qr.findById(q1ID).getAdditionalInfo());
        this.mockMvc.perform(post("/question/rename").param("id", String.valueOf(q1ID)).param("prompt","Changed")).andExpect(status().isFound());
        assertEquals("Changed",qr.findById(q1ID).getPrompt());
        this.mockMvc.perform(post("/question/changeInfo").param("id", String.valueOf(q1ID)).param("info","new info")).andExpect(status().isFound());
        assertEquals("new info",qr.findById(q1ID).getAdditionalInfo());

        // add option
        assertEquals(0,((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().size()); //control
        this.mockMvc.perform(post("/question/addOption").param("id", String.valueOf(q1ID)).param("option","DummyOption")).andExpect(status().isFound());
        assertEquals(1,((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().size());
        assertEquals("DummyOption",((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().get(0));

        //remove option
        //control above
        this.mockMvc.perform(post("/question/removeOption").param("id", String.valueOf(q1ID)).param("optionIndex","0")).andExpect(status().isFound());
        assertEquals(0,((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().size());
        log.info("Final result");
        this.mockMvc.perform(get("/question/" + q1ID)).andDo(print()).andExpect(status().isOk());

    }

    @SneakyThrows
    @Test
    public void testOAQ() {
        this.mockMvc.perform(get("/question/" + q2ID)).andDo(print()).andExpect(
                        status().isOk()).andExpect(
                        content().string(not(containsString("Question type not implemented"))))
                .andExpect(
                        content().string(containsString("Nothing else is needed for this type of question"))
                );


        // rename
        assertEquals("Open Answer Test Question", qr.findById(q2ID).getPrompt()); //control
        assertEquals("this project is based", qr.findById(q2ID).getAdditionalInfo());
        this.mockMvc.perform(post("/question/rename").param("id", String.valueOf(q2ID)).param("prompt", "Changed")).andExpect(status().isFound());
        assertEquals("Changed", qr.findById(q2ID).getPrompt());
        this.mockMvc.perform(post("/question/changeInfo").param("id", String.valueOf(q2ID)).param("info","new info")).andExpect(status().isFound());
        assertEquals("new info",qr.findById(q2ID).getAdditionalInfo());
    }


    @SneakyThrows
    @Test
    public void testRange(){
        this.mockMvc.perform(get("/question/" + q3ID)).andDo(print()).andExpect(status().isOk());

        // rename
        assertEquals("Range Test Question",qr.findById(q3ID).getPrompt()); //control
        assertEquals("this project is based",qr.findById(q3ID).getAdditionalInfo());
        this.mockMvc.perform(post("/question/rename").param("id", String.valueOf(q3ID)).param("prompt","Changed Range")).andExpect(status().isFound());
        assertEquals("Changed Range",qr.findById(q3ID).getPrompt());
        this.mockMvc.perform(post("/question/changeInfo").param("id", String.valueOf(q3ID)).param("info","new info")).andExpect(status().isFound());
        assertEquals("new info",qr.findById(q3ID).getAdditionalInfo());
        RangeQuestion rangeQuestion= (RangeQuestion)qr.findById(q3ID);
        //check default bounds
        assertEquals(0.0f, rangeQuestion.getLow(),0.001f);
        assertEquals(0.0f, rangeQuestion.getHigh(),0.001f);
        //set bounds
        this.mockMvc.perform(post("/question/setBounds")
                .param("lower","-10.0")
                .param("upper","10.0")
                .param("id", String.valueOf(q3ID))).andExpect(status().isFound());
        rangeQuestion= (RangeQuestion)qr.findById(q3ID);
        assertEquals(-10.0f, rangeQuestion.getLow(), 0.001f);
        assertEquals(10.0f, rangeQuestion.getHigh(), 0.001f);
        //set invalid bounds
        this.mockMvc.perform(post("/question/setBounds")
                .param("lower","-10")
                .param("upper","-200.0")
                .param("id", String.valueOf(q3ID))).andExpect(status().isFound());
        rangeQuestion= (RangeQuestion)qr.findById(q3ID);
        assertEquals(-10.0f, rangeQuestion.getLow(), 0.001f);
        assertEquals(-10.0f, rangeQuestion.getHigh(), 0.001f);

        log.info("Final result");
        this.mockMvc.perform(get("/question/" + q1ID)).andDo(print()).andExpect(status().isOk());

    }
    @SneakyThrows
    @Test
    public void testUneditingMcq(){
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) qr.findById(q1ID);
        List<String> options = new ArrayList<>();
        options.add("option1");
        mcq.setOptions(options);
        qr.save(mcq);
        Survey s = sr.findSurveyById(1);
        s.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(s);

        this.mockMvc.perform(get("/question/" + q1ID)).andDo(print()).andExpect(status().isOk());

        // rename
        assertEquals("Test Question",qr.findById(q1ID).getPrompt()); //control
        assertEquals("this project is based",qr.findById(q1ID).getAdditionalInfo());
        this.mockMvc.perform(post("/question/rename").param("id", String.valueOf(q1ID)).param("prompt","Changed"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals("Test Question",qr.findById(q1ID).getPrompt());
        this.mockMvc.perform(post("/question/changeInfo").param("id", String.valueOf(q1ID)).param("info","new info"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals("this project is based",qr.findById(q1ID).getAdditionalInfo());

        // add option
        assertEquals(1,((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().size()); //control
        this.mockMvc.perform(post("/question/addOption").param("id", String.valueOf(q1ID)).param("option","DummyOption"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals(1,((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().size());
        assertEquals("option1",((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().get(0));

        //remove option
        //control above
        this.mockMvc.perform(post("/question/removeOption").param("id", String.valueOf(q1ID)).param("optionIndex","0"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals(1,((MultipleChoiceQuestion)qr.findById(q1ID)).getOptions().size());
        log.info("Final result");
        this.mockMvc.perform(get("/question/" + q1ID)).andDo(print()).andExpect(status().isOk());

    }

    @SneakyThrows
    @Test
    public void testUneditingOAQ() {
        Survey s = sr.findSurveyById(1);
        s.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(s);

        this.mockMvc.perform(get("/question/" + q2ID)).andDo(print()).andExpect(
                        status().isOk()).andExpect(
                        content().string(not(containsString("Question type not implemented"))))
                .andExpect(
                        content().string(containsString("Nothing else is needed for this type of question"))
                );

        // rename
        assertEquals("Open Answer Test Question", qr.findById(q2ID).getPrompt()); //control
        assertEquals("this project is based", qr.findById(q2ID).getAdditionalInfo());
        this.mockMvc.perform(post("/question/rename").param("id", String.valueOf(q2ID)).param("prompt", "Changed"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals("Open Answer Test Question", qr.findById(q2ID).getPrompt());
        this.mockMvc.perform(post("/question/changeInfo").param("id", String.valueOf(q2ID)).param("info","new info"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals("this project is based",qr.findById(q2ID).getAdditionalInfo());
    }


    @SneakyThrows
    @Test
    public void testUneditingRange(){
        Survey s = sr.findSurveyById(1);
        s.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        sr.save(s);

        this.mockMvc.perform(get("/question/" + q3ID)).andDo(print()).andExpect(status().isOk());

        // rename
        assertEquals("Range Test Question",qr.findById(q3ID).getPrompt()); //control
        assertEquals("this project is based",qr.findById(q3ID).getAdditionalInfo());
        this.mockMvc.perform(post("/question/rename").param("id", String.valueOf(q3ID)).param("prompt","Changed Range"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals("Range Test Question",qr.findById(q3ID).getPrompt());
        this.mockMvc.perform(post("/question/changeInfo").param("id", String.valueOf(q3ID)).param("info","new info"))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        assertEquals("this project is based",qr.findById(q3ID).getAdditionalInfo());
        RangeQuestion rangeQuestion= (RangeQuestion)qr.findById(q3ID);
        //check default bounds
        assertEquals(0.0f, rangeQuestion.getLow(),0.001f);
        assertEquals(0.0f, rangeQuestion.getHigh(),0.001f);
        //set bounds
        this.mockMvc.perform(post("/question/setBounds")
                .param("lower","-10.0")
                .param("upper","10.0")
                .param("id", String.valueOf(q3ID)))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        rangeQuestion= (RangeQuestion)qr.findById(q3ID);
        assertEquals(0.0f, rangeQuestion.getLow(), 0.001f);
        assertEquals(0.0f, rangeQuestion.getHigh(), 0.001f);
        //set invalid bounds
        this.mockMvc.perform(post("/question/setBounds")
                .param("lower","-10")
                .param("upper","-200.0")
                .param("id", String.valueOf(q3ID)))
                .andExpect(content().string(containsString("Associated survey is not open for editing")))
                .andExpect(status().isOk());
        rangeQuestion= (RangeQuestion)qr.findById(q3ID);
        assertEquals(0.0f, rangeQuestion.getLow(), 0.001f);
        assertEquals(0.0f, rangeQuestion.getHigh(), 0.001f);

        log.info("Final result");
        this.mockMvc.perform(get("/question/" + q1ID)).andDo(print()).andExpect(status().isOk());

    }

    @SneakyThrows
    @Test
    public void testHtmxDelete(){
        //make sure webpage is up and contains question (control)
        this.mockMvc.perform(get("/survey/1/edit")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("/question/1")));

        //call controller
        this.mockMvc.perform(delete("/question/delete/1")).andExpect(status().isOk());


    }
}