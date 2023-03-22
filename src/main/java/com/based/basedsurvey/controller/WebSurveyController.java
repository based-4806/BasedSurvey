package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Log
@Controller
public class WebSurveyController {

    SurveyRepository surveyRepository;
    QuestionRepository questionRepository;
    @Autowired
    public WebSurveyController(SurveyRepository surveyRepository, QuestionRepository questionRepository){
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;

    }

    @GetMapping(path = "/")
    public String homePage(Model model) {
        model.addAttribute("surveys", surveyRepository.findAll());
        return "index";
    }

    @PostMapping("survey/create")
    public String createSurvey(@RequestParam String name) {
        surveyRepository.save(new Survey(name));
        return "redirect:/";
    }

    @PostMapping("survey/delete")
    public String deleteSurvey(@RequestParam Long id) {
        surveyRepository.deleteById(id);
        return "redirect:/";
    }

    @DeleteMapping("survey/{id}")
    @ResponseBody
    public String deleteSurveyHtmx(@PathVariable long id){
        surveyRepository.deleteById(id);
        return "";
    }
}
