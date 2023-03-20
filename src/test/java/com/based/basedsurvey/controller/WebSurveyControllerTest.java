package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.SurveyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bobby Portis jr")));;
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
}
