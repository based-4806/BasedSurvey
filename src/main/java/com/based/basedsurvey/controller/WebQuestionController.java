package com.based.basedsurvey.controller;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log
@Controller
public class WebQuestionController {
    private final String MULTIPLE_CHOICE_SHORT = "MQC";

    SurveyRepository surveyRepository;
    QuestionRepository questionRepository;
    @Autowired
    public WebQuestionController(SurveyRepository surveyRepository, QuestionRepository questionRepository){
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;

    }

    @GetMapping(path = "question/{id}")
    public String editQuestion(@PathVariable("id") long id, Model model) {
        var question = getQuestion(id);
        fillModel(model, question);

        return "EditQuestion";
    }

    @PostMapping("question/rename")
    public String editName(@RequestParam String prompt, @RequestParam long id) {
        var question = getQuestion(id);
        question.setPrompt(prompt);
        questionRepository.save(question);

        return "redirect:/question/"+id;
    }

    @PostMapping("question/removeOption")
    public String removeOption(@RequestParam int optionIndex, @RequestParam long id) {
        var mcq = getMCQ(id);
        var options = mcq.getOptions();
        if(optionIndex>=options.size()){
            throw new IllegalArgumentException(optionIndex+" is out of range of "+options.size());
        }
        options.remove(optionIndex);

        questionRepository.save(mcq);

        return "redirect:/question/"+id;
    }

    @PostMapping("question/addOption")
    public String addOption(@RequestParam @NonNull String option, @RequestParam long id){
        var mcq = getMCQ(id);
        mcq.getOptions().add(option);

        questionRepository.save(mcq);
        return "redirect:/question/"+id;
    }

    /**
     * uses ORM to get a multiple choice question
     * @param id the id of the question
     * @return MultiplechoiceQuestionz for that id
     */
    private MultiplechoiceQuestion getMCQ(long id){
        var question = getQuestion(id);
        if(!(question instanceof MultiplechoiceQuestion)){
            throw new IllegalArgumentException("Question of ID:" + id + " is not a multiple choice question");
        }
        return (MultiplechoiceQuestion)question;
    }

    /**
     * Uses ORM to find a question. If it doesn't exist, an exception is thrown
     * @param id the id of the question
     * @return the question
     */
    private Question getQuestion(long id){
        var question = questionRepository.findById(id);
        if(question == null){
            log.warning("Question with ID: "+id+" does not exist");
            throw new ResourceNotFoundException();
        }
        return question;
    }

    /**
     * Fills a model with everything it needs to display editing a question
     * @param model the model
     * @param question the question
     */
    private void fillModel(Model model, Question question){
        model.addAttribute("survey", question.getSurvey());
        model.addAttribute("question", question);
        List<String> options = new ArrayList<>();
        String type = "";
        if(question instanceof MultiplechoiceQuestion){
            options = ((MultiplechoiceQuestion)question).getOptions();
            type = MULTIPLE_CHOICE_SHORT;
        }else {
            log.warning(question+" type not implemented");
        }
        model.addAttribute("type", type);
        model.addAttribute("options", options);

    }
}