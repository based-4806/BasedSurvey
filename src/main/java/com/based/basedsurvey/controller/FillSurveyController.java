package com.based.basedsurvey.controller;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.extern.java.Log;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Log
@Controller
public class FillSurveyController {

    private SurveyRepository surveyRepository;

    @Autowired
    public FillSurveyController(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @GetMapping("/survey/{surveyID}/answer")
    public String getAnswer(@PathVariable Long surveyID, Model model) {
        // if survey does not exist
        if (!surveyRepository.existsById(surveyID)) {
            model.addAttribute("issue", "Survey does not exist");
            return "SurveyFillIssue";
        }

        model.addAttribute("surveyID", surveyID);
        Survey s = this.surveyRepository.findSurveyById(surveyID);
        model.addAttribute("surveyName", s.getName());

        // if survey is closed
        if (!s.isOpen()) {
            model.addAttribute("issue", "Survey is closed");
            return "SurveyFillIssue";
        }

        // if survey has no questions
        if (s.getQuestions().size() == 0) {
            model.addAttribute("issue", "Survey has no questions");
            return "SurveyFillIssue";
        }

        // formInputs will contain a dynamically generated string with all the html form inputs
        String formInputs = "";

        // for each question in survey
        for (Question q : s.getQuestions()) {

            // case 1, the question is a multiple choice question
            if (q instanceof MultipleChoiceQuestion) {
                formInputs += getMultipleChoiceInputs((MultipleChoiceQuestion) q);

                // case 2, the question is an open answer question
            } else if (q instanceof OpenAnswerQuestion) {
                formInputs += getOpenAnswerInput((OpenAnswerQuestion) q);

                // case 3, the question is a range question
            } else if (q instanceof RangeQuestion) {
                formInputs += getRangeInput((RangeQuestion) q);
            }
        }

        model.addAttribute("questions", formInputs);
        return "Answer";
    }

    // side note, html forms can't send patch requests and I used post instead
    @PostMapping("/survey/{surveyID}/answer")
    public String postAnswer(@PathVariable Long surveyID, @RequestParam List<String> values, Model model) {

        // if survey does not exist
        if (!surveyRepository.existsById(surveyID)) {
            model.addAttribute("issue", "Survey does not exist");
            return "SurveyFillIssue";
        }

        Survey s = this.surveyRepository.findSurveyById(surveyID);

        // for each question in survey add the response
        for (int index = 0; index < s.getQuestions().size(); index += 1) {
            Question q = s.getQuestions().get(index);
            addResponse(q, values.get(index));
        }

        surveyRepository.save(s);

        // return to home page
        return "redirect:/";
    }

    /**
     * Adds the response to a question
     * @param q the question to add the response to
     * @param value the value of the response
     */
    private void addResponse(Question q, String value) {

        //case 1, the question is a multiple choice question
        if (q instanceof MultipleChoiceQuestion multiChoice) {
            multiChoice.getResponses().add(((MultipleChoiceQuestion) q).getOptions().indexOf(value));

            // case 2, the question is an open answer question
        } else if (q instanceof OpenAnswerQuestion openAnswer) {
            openAnswer.getResponses().add(value);

            // case 3, the question is a range question
        } else if (q instanceof RangeQuestion rangeAnswer) {
            rangeAnswer.getResponses().add(Float.parseFloat(value));
        }
    }

    /**
     * Generates the HTML inputs for a multiple choice question
     * @param q the multiple choice question
     * @return the HTML inputs for the question
     */
    private String getMultipleChoiceInputs(MultipleChoiceQuestion q) {
        @Language("html")
        String s = "<hr>";
        s += "<h3> " + q.getPrompt() + " </h3>";
        if (!q.getAdditionalInfo().isEmpty()) s += "additional notes: " + q.getAdditionalInfo() + "<br>";
        for (String option : (q).getOptions()) {
            s += "<input type=\"radio\" id=\"" + option + "\" name=\"values\" checked=\"checked\" value=\"" + option + "\">";
            s += "<label for=\"" + option + "\">" + option + "</label><br>";
        }
        s += "<br>";
        return s;
    }

    /**
     * Generates the HTML inputs for an open answer question
     * @param q the open answer question
     * @return the HTML inputs for the question
     */
    private String getOpenAnswerInput(OpenAnswerQuestion q) {
        @Language("html")
        String s = "<hr>";
        s += "<h3>" + q.getPrompt() + "</h3>";
        if (!q.getAdditionalInfo().isEmpty()) s += "additional notes: " + q.getAdditionalInfo() + "<br>";
        s += "<input type=\"text\" id=\"values\" name=\"values\"><br>";
        return s;
    }

    /**
     * Generates the HTML inputs for a range question
     * @param q the range question
     * @return the HTML inputs for the question
     */
    private String getRangeInput(RangeQuestion q) {
        @Language("html")
        String s = "<hr>";
        s += "<h3>" + q.getPrompt() + "</h3>";
        if (!q.getAdditionalInfo().isEmpty()) s += "additional notes: " + q.getAdditionalInfo() + "<br>";
        s += "<input type=\"range\" name=\"values\" value=\"" + (q).getLow() + "\" min=\"" + (q).getLow() + "\" max=\"" + (q).getHigh() + "\" step=\"0.1\" oninput=\"this.nextElementSibling.value = this.value\">\n";
        s += "<output>" + (q).getLow() + "</output>";
        return s;
    }
}

