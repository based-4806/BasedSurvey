package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.MultiplechoiceQuestion;
import com.based.basedsurvey.data.Question;
import com.based.basedsurvey.data.QuestionTypes;
import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        var survey = getSurvey(id);
        model.addAttribute("questions", questionRepository.findAllBySurveyId(survey.getId()));
        model.addAttribute("survey", survey);
        return "newSurvey";
    }

    @PostMapping("question/create")
    public String createQuestion(@RequestParam long surveyID, @RequestParam QuestionTypes qt, @RequestParam String prompt, @RequestParam String additionalInfo) {
        var question = QuestionTypes.makeQuestionFromType(qt);
        var survey = getSurvey(surveyID);
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
        var survey = getSurvey(surveyID);
        var question = getQuestion(questionID);
        survey.getQuestions().remove(question);
        surveyRepository.save(survey);
        return "redirect:/survey/"+surveyID +"/edit";
    }

    @DeleteMapping(value = "question/delete/{questionID}")
    @ResponseBody
    public String deleteSurveyHtmx(@PathVariable long questionID){
        var question = getQuestion(questionID);
        var survey = question.getSurvey();
        survey.getQuestions().remove(question);
        surveyRepository.save(survey);
        return "";
    }

    @PostMapping("survey/rename")
    public String editName(@RequestParam String prompt, @RequestParam long id) {
        var survey = getSurvey(id);
        survey.setName(prompt);
        surveyRepository.save(survey);
        return "redirect:/survey/"+id +"/edit";
    }

    @PostMapping("survey/openSurvey")
    public String enableButtons(@RequestParam long id, @RequestParam boolean enable) {
        var survey = getSurvey(id);
        survey.setOpen(enable);
        surveyRepository.save(survey);
        return "redirect:/survey/"+id +"/edit";
    }


    private Survey getSurvey(long id){
        var survey = surveyRepository.findSurveyById(id);
        if(survey == null){
            log.warning("Survey with ID: "+id+" does not exist");
            throw new ResourceNotFoundException();
        }
        return survey;
    }

    private Question getQuestion(long id){
        var question = questionRepository.findById(id);
        if(question == null){
            log.warning("Question with ID: "+id+" does not exist");
            throw new ResourceNotFoundException();
        }
        return question;
    }
}
