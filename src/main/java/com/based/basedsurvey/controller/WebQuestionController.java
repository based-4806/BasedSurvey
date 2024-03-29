package com.based.basedsurvey.controller;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log
@Controller
public class WebQuestionController {
    SurveyRepository surveyRepository;
    QuestionRepository questionRepository;
    @Autowired
    public WebQuestionController(SurveyRepository surveyRepository, QuestionRepository questionRepository){
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;

    }

    @GetMapping(path = "question/{id}")
    public String editQuestion(@PathVariable("id") long id, Model model) {
        var question = ControllerHelperClass.getQuestion(id);
        if (question.getSurvey().getStatus() != Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("surveyName", question.getSurvey().getName());
            model.addAttribute("issue", "Associated survey is not open for editing");
            return "QuestionError";
        }
        fillModel(model, question);

        return "EditQuestion";
    }

    @PostMapping("question/rename")
    public String editName(@RequestParam String prompt, @RequestParam long id, Model model) {
        var question = ControllerHelperClass.getQuestion(id);
        if (question.getSurvey().getStatus() != Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("surveyName", question.getSurvey().getName());
            model.addAttribute("issue", "Associated survey is not open for editing");
            return "QuestionError";
        }
        question.setPrompt(prompt);
        questionRepository.save(question);

        return "redirect:/question/"+id;
    }

    @PostMapping("question/changeInfo")
    public String editAdditionalInfo(@RequestParam String info, @RequestParam long id, Model model) {
        var question = ControllerHelperClass.getQuestion(id);
        if (question.getSurvey().getStatus() != Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("surveyName", question.getSurvey().getName());
            model.addAttribute("issue", "Associated survey is not open for editing");
            return "QuestionError";
        }
        question.setAdditionalInfo(info);
        questionRepository.save(question);

        return "redirect:/question/"+id;
    }

    @PostMapping("question/removeOption")
    public String removeOption(@RequestParam int optionIndex, @RequestParam long id, Model model) {
        var mcq = getMCQ(id);
        if (mcq.getSurvey().getStatus() != Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("surveyName", mcq.getSurvey().getName());
            model.addAttribute("issue", "Associated survey is not open for editing");
            return "QuestionError";
        }
        var options = mcq.getOptions();
        if(optionIndex>=options.size()){
            throw new IllegalArgumentException(optionIndex+" is out of range of "+options.size());
        }
        options.remove(optionIndex);

        questionRepository.save(mcq);

        return "redirect:/question/"+id;
    }

    @PostMapping("question/addOption")
    public String addOption(@RequestParam @NonNull String option, @RequestParam long id, Model model){
        var mcq = getMCQ(id);
        if (mcq.getSurvey().getStatus() != Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("surveyName", mcq.getSurvey().getName());
            model.addAttribute("issue", "Associated survey is not open for editing");
            return "QuestionError";
        }
        mcq.getOptions().add(option);

        questionRepository.save(mcq);
        return "redirect:/question/"+id;
    }

    @PostMapping("question/setBounds")
    public String setBounds(@RequestParam float lower, @RequestParam float upper, @RequestParam long id, Model model){
        var rq = getRQ(id);
        if (rq.getSurvey().getStatus() != Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("surveyName", rq.getSurvey().getName());
            model.addAttribute("issue", "Associated survey is not open for editing");
            return "QuestionError";
        }
        if(upper<lower){
            upper = lower;
        }
        rq.setHigh(upper);
        rq.setLow(lower);
        questionRepository.save(rq);

        return "redirect:/question/"+id;
    }

    /**
     * uses ORM to get a multiple choice question
     * @param id the id of the question
     * @return MultiplechoiceQuestionz for that id
     */
    private MultipleChoiceQuestion getMCQ(long id){
        var question = ControllerHelperClass.getQuestion(id);
        if(!(question instanceof MultipleChoiceQuestion)){
            throw new IllegalArgumentException("Question of ID:" + id + " is not a multiple choice question");
        }
        return (MultipleChoiceQuestion)question;
    }

    /**
     * uses ORM to get a range question
     * @param id the id of the question
     * @return the range  for that id
     */
    private RangeQuestion getRQ(long id){
        var question = ControllerHelperClass.getQuestion(id);
        if(!(question instanceof RangeQuestion)){
            throw new IllegalArgumentException("Question of ID:" + id + " is not a range question");
        }
        return (RangeQuestion)question;
    }

    /**
     * Fills a model with everything it needs to display editing a question
     * @param model the model
     * @param question the question
     */
    private void fillModel(Model model, Question question) {
        model.addAttribute("survey", question.getSurvey());
        model.addAttribute("question", question);
        List<String> options = new ArrayList<>();
        var type = QuestionTypes.OPEN_ENDED;
        float lowerBound = 0, upperBound = 0;
        if (question instanceof MultipleChoiceQuestion) {
            options = ((MultipleChoiceQuestion) question).getOptions();
            type = QuestionTypes.MULTIPLE_CHOICE;
        } else if (question instanceof OpenAnswerQuestion) {
            type = QuestionTypes.OPEN_ENDED;
        }else if (question instanceof RangeQuestion rq) {
            type = QuestionTypes.RANGE;
            lowerBound = rq.getLow();
            upperBound = rq.getHigh();
        } else {
            log.warning(question+" type not implemented");
        }
        model.addAttribute("type", type);
        model.addAttribute("options", options);
        model.addAttribute("lower", lowerBound);
        model.addAttribute("upper", upperBound);

    }
}