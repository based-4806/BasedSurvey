package com.based.basedsurvey.data;

import com.based.basedsurvey.repo.QuestionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class MultipleChoiceQuestionTest {
    @Autowired
    private QuestionRepository repo;

    private MultipleChoiceQuestion question;
    private MultipleChoiceQuestion questionUnderTest;

    @Before
    public void setup(){
        repo.deleteAll();
        question = new MultipleChoiceQuestion();
        question.setPrompt("Prompt Text");
        question.setAdditionalInfo("Some Additional Info");
        var options = question.getOptions();
        options.add("option 1");
        options.add("option 2");
        options.add("option 3");
        var responses = question.getResponses();
        responses.add(0);
        responses.add(0);
        repo.save(question);
        questionUnderTest = (MultipleChoiceQuestion)repo.findById(question.getId());
    }


    @Test
    public void testPrompt(){
        assertEquals("Prompt Text", questionUnderTest.getPrompt());
    }

    @Test
    public void testAdditionalInfo(){
        assertEquals("Some Additional Info", questionUnderTest.getAdditionalInfo());
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