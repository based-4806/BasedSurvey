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
public class MultiplechoiceQuestionTest {
    @Autowired
    private QuestionRepository repo;

    private MultiplechoiceQuestion question;
    private MultiplechoiceQuestion questionUnderTest;

    @Before
    public void setup(){
        repo.deleteAll();
        question = new MultiplechoiceQuestion();
        question.setPrompt("Prompt Text");
        var options = question.getOptions();
        options.add("option 1");
        options.add("option 2");
        options.add("option 3");
        var responses = question.getResponses();
        responses.add(0);
        responses.add(0);
        repo.save(question);
        questionUnderTest = (MultiplechoiceQuestion)repo.findById(question.getId());
    }


    @Test
    public void testPrompt(){
        assertEquals("Prompt Text", questionUnderTest.getPrompt());
    }

    @Test
    public void testOptions(){
        assertEquals(3, questionUnderTest.getOptions().size());
        assertEquals("option 1", questionUnderTest.getOptions().get(0));
        assertEquals("option 2", questionUnderTest.getOptions().get(1));
        assertEquals("option 3", questionUnderTest.getOptions().get(2));
    }
    @Test
    public void testResponses(){
        assertEquals(2, questionUnderTest.getResponses().size());
        assertTrue(0 == questionUnderTest.getResponses().get(0));
        assertTrue(0 == questionUnderTest.getResponses().get(1));
    }


}