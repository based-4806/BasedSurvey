package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.SurveyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@SpringBootTest
@AutoConfigureMockMvc
public class ResultsSurveyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    @Test
    public void testInvalidSurveyIDResults() throws Exception{
        mockMvc.perform(get("/survey/9999/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Survey does not exist")));
    }

    @Test
    public void testEmptyQuestionResults() throws Exception{
        String name = "survey1";
        Survey s = new Survey(name);
        surveyRepository.save(s);
        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString("Survey has no questions")));
    }

    @Test
    public void testEmptyMultipleChoiceResults() throws Exception{
        List<Question> questions = new ArrayList<>();
        MultipleChoiceQuestion q = new MultipleChoiceQuestion();
        String prompt = "Choose A or B:";
        q.setPrompt(prompt);
        String additionalInfo = "this project is based";
        q.setAdditionalInfo(additionalInfo);
        String choice1 = "A";
        String choice2 = "B";

        List<String> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        q.setOptions(choices);

        questions.add(q);
        String name = "survey1";
        Survey s = new Survey(name);
        s.setQuestions(questions);
        surveyRepository.save(s);

        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(prompt)))
                .andExpect(content().string(containsString(additionalInfo)))
                .andExpect(content().string(containsString(choice1 + ": 0.00%")))
                .andExpect(content().string(containsString(choice2 + ": 0.00%")));
    }

    @Test
    public void testEmptyOpenAnswerResults() throws Exception{
        List<Question> questions = new ArrayList<>();
        OpenAnswerQuestion q = new OpenAnswerQuestion();
        String prompt = "Enter some text:";
        q.setPrompt(prompt);
        String additionalInfo = "this project is based";
        q.setAdditionalInfo(additionalInfo);

        questions.add(q);
        String name = "survey1";
        Survey s = new Survey(name);
        s.setQuestions(questions);
        surveyRepository.save(s);

        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(prompt)))
                .andExpect(content().string(containsString(additionalInfo)))
                .andExpect(content().string(containsString("No responses.")));
    }

    @Test
    public void testEmptyRangeResults() throws Exception{
        List<Question> questions = new ArrayList<>();
        String prompt = "Choose a value:";
        float low = -1f;
        float high = 6f;
        RangeQuestion q = new RangeQuestion(prompt, low, high);
        String additionalInfo = "this project is based";
        q.setAdditionalInfo(additionalInfo);

        questions.add(q);
        String name = "survey1";
        Survey s = new Survey(name);
        s.setQuestions(questions);
        surveyRepository.save(s);

        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(prompt)))
                .andExpect(content().string(containsString(additionalInfo)))
                .andExpect(content().string(containsString("No responses.")));
    }

    @Test
    public void testMultipleChoiceResults() throws Exception{
        List<Question> questions = new ArrayList<>();
        MultipleChoiceQuestion q = new MultipleChoiceQuestion();
        String prompt = "Choose A or B:";
        q.setPrompt(prompt);
        String additionalInfo = "this project is based";
        q.setAdditionalInfo(additionalInfo);
        String choice1 = "A";
        String choice2 = "B";

        List<String> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        q.setOptions(choices);

        List<Integer> responses = new ArrayList<>();
        responses.add(0);
        responses.add(0);
        responses.add(0);
        responses.add(0);
        responses.add(1);
        q.setResponses(responses);

        questions.add(q);
        String name = "survey1";
        Survey s = new Survey(name);
        s.setQuestions(questions);
        surveyRepository.save(s);

        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(prompt)))
                .andExpect(content().string(containsString(additionalInfo)))
                .andExpect(content().string(containsString(choice1 + ": 80.00%")))
                .andExpect(content().string(containsString(choice2 + ": 20.00%")));
    }

    @Test
    public void testOpenAnswerResults() throws Exception{
        List<Question> questions = new ArrayList<>();
        OpenAnswerQuestion q = new OpenAnswerQuestion();
        String prompt = "Enter some text:";
        q.setPrompt(prompt);
        String additionalInfo = "this project is based";
        q.setAdditionalInfo(additionalInfo);

        List<String> responses = new ArrayList<>();
        String response1 = "aaaaaaaaaaa";
        String response2 = "bbbbbbbbbbb";
        String response3 = "75867920137";
        responses.add(response1);
        responses.add(response2);
        responses.add(response3);
        q.setResponses(responses);

        questions.add(q);
        String name = "survey1";
        Survey s = new Survey(name);
        s.setQuestions(questions);
        surveyRepository.save(s);

        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(prompt)))
                .andExpect(content().string(containsString(additionalInfo)))
                .andExpect(content().string(containsString(response1)))
                .andExpect(content().string(containsString(response2)))
                .andExpect(content().string(containsString(response3)));
    }

    @Test
    public void testRangeResults() throws Exception{
        List<Question> questions = new ArrayList<>();
        String prompt = "Choose a value:";
        float low = -1f;
        float high = 6f;
        RangeQuestion q = new RangeQuestion(prompt, low, high);
        String additionalInfo = "this project is based";
        q.setAdditionalInfo(additionalInfo);

        List<Float> responses = new ArrayList<>();
        responses.add(-1f);
        responses.add(6f);
        responses.add(4f);
        responses.add(4f);
        responses.add(2f);
        q.setResponses(responses);

        questions.add(q);
        String name = "survey1";
        Survey s = new Survey(name);
        s.setQuestions(questions);
        surveyRepository.save(s);

        mockMvc.perform(get("/survey/1/results"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(prompt)))
                .andExpect(content().string(containsString(additionalInfo)))
                .andExpect(content().string(containsString("<tr><td>[-1.00, 0.00)</td><td>▬</td><td></td></tr>")))
                .andExpect(content().string(containsString("<tr><td>[4.00, 5.00)</td><td>▬</td><td>▬</td></tr>")));
    }


}
