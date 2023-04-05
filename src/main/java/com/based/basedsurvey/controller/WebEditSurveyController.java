package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.MultipleChoiceQuestion;
import com.based.basedsurvey.data.QuestionTypes;
import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@Log
@Controller
public class WebEditSurveyController {

    private SurveyRepository surveyRepository;
    private QuestionRepository questionRepository;
    @Autowired
    public WebEditSurveyController(SurveyRepository surveyRepository, QuestionRepository questionRepository){
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;

    }

    @GetMapping(path = "survey/{surveyId}/edit")
    public String editSurvey(@PathVariable("surveyId") long id, Model model) {
        var survey = ControllerHelperClass.getSurvey(id);
        model.addAttribute("questions", questionRepository.findAllBySurveyId(survey.getId()));
        model.addAttribute("survey", survey);
        model.addAttribute("surveyStatus", survey.getStatus());
        return "newSurvey";
    }

    @PostMapping("question/create")
    public String createQuestion(@RequestParam long surveyID, @RequestParam QuestionTypes qt, @RequestParam String prompt, @RequestParam String additionalInfo) {
        var question = QuestionTypes.makeQuestionFromType(qt);
        var survey = ControllerHelperClass.getSurvey(surveyID);
        question.setSurvey(survey);
        question.setPrompt(prompt);
        question.setAdditionalInfo(additionalInfo);
        questionRepository.save(question);
        survey.getQuestions().add(question);
        surveyRepository.save(survey);
        return "redirect:/survey/"+surveyID +"/edit";
    }

    @PostMapping("question/delete")
    public String deleteQuestion(@RequestParam long questionID, @RequestParam long surveyID) {
        var survey = ControllerHelperClass.getSurvey(surveyID);
        var question = ControllerHelperClass.getQuestion(questionID);
        survey.getQuestions().remove(question);
        surveyRepository.save(survey);
        return "redirect:/survey/"+surveyID +"/edit";
    }

    @DeleteMapping(value = "question/delete/{questionID}")
    @ResponseBody
    public String deleteSurveyHtmx(@PathVariable long questionID){
        var question = ControllerHelperClass.getQuestion(questionID);
        var survey = question.getSurvey();
        survey.getQuestions().remove(question);
        surveyRepository.save(survey);
        return "";
    }

    @PostMapping("survey/rename")
    public String editName(@RequestParam String prompt, @RequestParam long id) {
        var survey = ControllerHelperClass.getSurvey(id);
        survey.setName(prompt);
        surveyRepository.save(survey);
        return "redirect:/survey/"+id +"/edit";
    }

    @PostMapping("survey/openSurvey")
    public String enableButtons(@RequestParam long id) {
        var survey = ControllerHelperClass.getSurvey(id);
        survey.setStatus(Survey.SurveyStatuses.BEING_FILLED);
        var empties = survey.getQuestions().stream().filter(question -> (question instanceof MultipleChoiceQuestion) && (((MultipleChoiceQuestion) question).getOptions().isEmpty()) ).collect(Collectors.toList());
        questionRepository.deleteAll(empties);
        survey.getQuestions().removeAll(empties);
        surveyRepository.save(survey);
        return "redirect:/survey/"+id +"/edit";
    }

    @PostMapping("survey/finishSurvey")
    public String finishSurvey(@RequestParam long id) {
        var survey = ControllerHelperClass.getSurvey(id);
        survey.setStatus(Survey.SurveyStatuses.FINISHED);
        surveyRepository.save(survey);
        return "redirect:/";
    }

    @GetMapping("survey/htmx/rename/{id}")
    public String getEditName(@PathVariable long id,Model model){
        var survey = ControllerHelperClass.getSurvey(id);
        model.addAttribute("survey",survey);
        return "fragments/EditSurveyName::rename";
    }

    @PutMapping("survey/rename/{id}")
    public String rename(@PathVariable long id, @RequestParam @NonNull String name, Model model){
        var survey = ControllerHelperClass.getSurvey(id);
        survey.setName(name);
        surveyRepository.save(survey);
        model.addAttribute("survey",survey);
        return "fragments/EditSurveyName::default-name";
    }
}
