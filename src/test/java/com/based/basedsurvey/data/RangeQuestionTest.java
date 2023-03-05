package com.based.basedsurvey.data;

import com.based.basedsurvey.repo.QuestionRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RangeQuestionTest {
    @Autowired
    private QuestionRepository repo;

    private RangeQuestion question;
    private RangeQuestion questionUnderTest;

    @Before
    public void setup(){
        repo.deleteAll();
        question = new RangeQuestion();
        question.setPrompt("Prompt Text");
        question.setLow(0);
        question.setHigh(2);
        var responses = question.getResponses();
        responses.add(0.2f);
        responses.add(1.1f);
        repo.save(question);

        questionUnderTest = (RangeQuestion) repo.findById(question.getId());
    }
    @Test
    public void testBounds(){
        assertEquals(0, questionUnderTest.getLow(),0.01f);
        assertEquals(2, questionUnderTest.getHigh(),0.01f);
        assertTrue(questionUnderTest.isInRange(0));
        assertTrue(questionUnderTest.isInRange(2));
        assertFalse(questionUnderTest.isInRange(-0.1f));
        assertFalse(questionUnderTest.isInRange(2.1f));
    }
    @Test
    public void testPrompt(){
        assertEquals("Prompt Text", questionUnderTest.getPrompt());
    }

    @Test
    public void testResponses(){
        assertEquals(2, questionUnderTest.getResponses().size());
        assertTrue(0.2f == questionUnderTest.getResponses().get(0));
        assertTrue(1.1f == questionUnderTest.getResponses().get(1));
    }
}