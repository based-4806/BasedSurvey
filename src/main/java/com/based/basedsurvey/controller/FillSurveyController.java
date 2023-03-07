package com.based.basedsurvey.controller;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class FillSurveyController {

    SurveyRepository surveyRepository;

    @Autowired
    public FillSurveyController(SurveyRepository surveyRepository){
        this.surveyRepository = surveyRepository;
    }

    @GetMapping("/survey/{surveyID}/answer")
    public String getAnswer(@PathVariable String surveyID, Model model) {
        model.addAttribute("surveyID", surveyID);
        Survey s = surveyRepository.findSurveyById(Long.parseLong(surveyID));

        model.addAttribute("surveyName", s.getName());


        if(s.getQuestions().size() == 0){
            model.addAttribute("empty_or_not", true);
            return "Answer";
        }

        // formInputs will contain a dynamically generated string with all the html form inputs
        String formInputs = "";

        // for each question in survey
        for(int index = 0; index < s.getQuestions().size(); index+=1){
            Question q = s.getQuestions().get(index);

            // case 1, the question is a multiple choice question
            if(q instanceof MultiplechoiceQuestion){
                formInputs += "<h3> "+q.getPrompt()+" </h3>";
                for(String option: ((MultiplechoiceQuestion) q).getOptions()){
                    formInputs += "<input type=\"radio\" id=\""+option+"\" name=\"values\" checked=\"checked\" value=\""+option+"\">";
                    formInputs += "<label for=\""+option+"\">"+option+"</label><br>";
                }
                formInputs += "<br>";

                // case 2, the question is an open answer question
            }else if(q instanceof OpenAnswerQuestion){
                formInputs += "<h3>"+q.getPrompt()+"</h3>";
                formInputs += "<input type=\"text\" id=\"values\" name=\"values\"><br>";

                // case 3, the question is a range question
            }else if(q instanceof RangeQuestion){
                formInputs += "<h3>"+q.getPrompt()+"</h3>";
                formInputs += "<input type=\"range\" name=\"values\" value=\""+((RangeQuestion) q).getLow()+"\" min=\""+((RangeQuestion) q).getLow()+"\" max=\""+((RangeQuestion) q).getHigh()+"\" step=\"0.1\" oninput=\"this.nextElementSibling.value = this.value\">\n";
                formInputs += "<output>"+((RangeQuestion) q).getLow()+"</output>";
            }
        }

        model.addAttribute("empty_or_not", false);
        model.addAttribute("questions", formInputs);
        return "Answer";
    }

    // side note, html forms can't send patch requests and I used post instead
    @PostMapping("/survey/{surveyID}/answer")
    public String postAnswer(@PathVariable String surveyID, @RequestParam List<String> values, Model model) {
        System.out.println("GOT HERE");
        Survey s = surveyRepository.findSurveyById(Long.parseLong(surveyID));

        // for each question in survey
        for(int index = 0; index < s.getQuestions().size(); index+=1){
            Question q = s.getQuestions().get(index);

            // case 1, the question is a multiple choice question
            if(q instanceof MultiplechoiceQuestion multiChoice){
                multiChoice.getResponses().add(((MultiplechoiceQuestion) q).getOptions().indexOf(values.get(index)));

                // case 2, the question is an open answer question
            }else if(q instanceof OpenAnswerQuestion openAnswer){
                openAnswer.getResponses().add(values.get(index));

                // case 3, the question is a range question
            }else if(q instanceof RangeQuestion rangeAnswer){
                rangeAnswer.getResponses().add(Float.parseFloat(values.get(index)));
            }
        }

        // return to home page
        model.addAttribute("surveys", surveyRepository.findAll());
        return "index";
    }
}

