package com.based.basedsurvey.data;

import com.based.basedsurvey.repo.QuestionRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OpenAnswerQuestionTest {
    @Autowired
    private QuestionRepository repo;

    private OpenAnswerQuestion question;
    private OpenAnswerQuestion questionUnderTest;

    @Before
    public void setup(){
        repo.deleteAll();
        question = new OpenAnswerQuestion();
        question.setPrompt("Prompt Text");
        var responses = question.getResponses();
        responses.add("Answer 1");
        responses.add("Answer 2");
        repo.save(question);

        questionUnderTest = (OpenAnswerQuestion) repo.findById(question.getId());
    }
    @After
    public void close(){
        repo.deleteAll();
    }

    @Test
    public void testPrompt(){
        assertEquals("Prompt Text", questionUnderTest.getPrompt());
    }

    @Test
    public void testResponses(){
        assertEquals(2, questionUnderTest.getResponses().size());
        assertEquals("Answer 1", questionUnderTest.getResponses().get(0));
        assertEquals("Answer 2", questionUnderTest.getResponses().get(1));
    }
}