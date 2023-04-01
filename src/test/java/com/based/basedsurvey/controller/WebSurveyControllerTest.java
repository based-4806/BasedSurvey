package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.not;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@SpringBootTest
@AutoConfigureMockMvc
public class WebSurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;
    @Test
    public void testSurveyList() throws Exception{
        surveyRepository.save(new Survey("Bobby Portis jr"));
        mockMvc.perform(get("/surveys/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bobby Portis jr")));
    }

    @Test
    public void testCreateSurvey() throws Exception{
        long size = surveyRepository.findAll().spliterator().estimateSize();
        mockMvc.perform(post("/survey/create").param("name", "based"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        Assertions.assertEquals(size + 1, surveyRepository.findAll().spliterator().estimateSize());
    }

    @Test
    @SneakyThrows
    public void testSurveyBeingFilledIndex(){
        var survey = new Survey("Bobby Portis jr");
        survey.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        surveyRepository.save(survey);
        mockMvc.perform(get("/surveys/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Survey cannot be edited")))
                .andExpect(content().string(containsString("BEING FILLED")))
                .andExpect(content().string(not(containsString("BEING EDITED"))))
                .andExpect(content().string(not(containsString("FINISHED"))));
    }

    @Test
    @SneakyThrows
    public void testBeingEditedSurveyIndex(){
        var survey = new Survey("Bobby Portis jr");
        survey.setStatus(Survey.SurveyStatuses.BEING_EDITED);
        surveyRepository.save(survey);
        mockMvc.perform(get("/surveys/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not((containsString("Survey cannot be edited")))))
                .andExpect(content().string(containsString("BEING EDITED")))
                .andExpect(content().string(not(containsString("BEING FILLED"))))
                .andExpect(content().string(not(containsString("FINISHED"))));
    }

    @Test
    public void testDeleteSurvey() throws Exception{
        Survey s1 = new Survey("Bobby Portis jr");
        surveyRepository.save(s1);
        long size = surveyRepository.findAll().spliterator().estimateSize();
        mockMvc.perform(post("/survey/delete").param("id", (String.valueOf(s1.getId()))))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        Assertions.assertEquals(size - 1, surveyRepository.findAll().spliterator().estimateSize());
    }

    @Test
    @SneakyThrows
    public void testHtmxDeleteSurvey(){
        Survey s1 = new Survey("Bobby Portis jr");
        surveyRepository.save(s1);
        long size = surveyRepository.findAll().spliterator().estimateSize();
        mockMvc.perform(delete("/survey/"+s1.getId()))
                .andDo(print())
                .andExpect(status().isOk());
        Assertions.assertEquals(size - 1, surveyRepository.findAll().spliterator().estimateSize());
    }

    @Test
    public void testPagination() throws Exception {
        for (int i = 0; i < 10; i++) {
            surveyRepository.save(new Survey("hello " + i));
        }
        surveyRepository.save(new Survey("bye"));
        mockMvc.perform(get("/surveys/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bye")))
                .andExpect(content().string(not(containsString("hello"))));

        mockMvc.perform(get("/surveys/0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("bye"))))
                .andExpect(content().string(containsString("hello")));
    }

}
