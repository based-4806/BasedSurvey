package com.based.basedsurvey.controller;
import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.extern.java.Log;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log
@Controller
public class FillSurveyController {

    SurveyRepository surveyRepository;
    QuestionRepository questionRepository;

    private final int pageSize = 3;


    @Autowired
    public FillSurveyController(SurveyRepository surveyRepository, QuestionRepository questionRepository) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
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

        // if survey is still being edited
        if (s.getStatus() == Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("issue", "Survey is being edited");
            return "SurveyFillIssue";
        }

        // if survey is still finished
        if (s.getStatus() == Survey.SurveyStatuses.FINISHED) {
            model.addAttribute("issue", "Survey is finished");
            return "SurveyFillIssue";
        }

        // if survey has no questions
        if (s.getQuestions().size() == 0) {
            model.addAttribute("issue", "Survey has no questions");
            return "SurveyFillIssue";
        }
        return "Answer";
    }

    @GetMapping("/survey/{surveyID}/answer/{page}")
    @ResponseBody
    public String getAnswer(@PathVariable Long surveyID, @PathVariable int page, Model model) {

        // formInputs will contain a dynamically generated string with all the html form inputs
        StringBuilder formInputs = new StringBuilder();
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("id").ascending());

        var questions = questionRepository.findAllBySurveyId(surveyID, pageRequest).getContent();
        if (questions.isEmpty()) return ""; // to prevent infinite requests from pages that have 0 elements

        // for each question in survey
        for (Question q : questions) {

            // case 1, the question is a multiple choice question
            if (q instanceof MultipleChoiceQuestion) {
                formInputs.append(getMultipleChoiceInputs((MultipleChoiceQuestion) q));

                // case 2, the question is an open answer question
            } else if (q instanceof OpenAnswerQuestion) {
                formInputs.append(getOpenAnswerInput((OpenAnswerQuestion) q));

                // case 3, the question is a range question
            } else if (q instanceof RangeQuestion) {
                formInputs.append(getRangeInput((RangeQuestion) q));
            }
        }
        formInputs.append(getPaginateHtml(surveyID, page + 1));
        return formInputs.toString();
    }

    // side note, html forms can't send patch requests and I used post instead
    @PostMapping("/survey/{surveyID}/answer")
    public String postAnswer(@PathVariable Long surveyID, @RequestParam Map<String, String> allParams, Model model) {

        // if survey does not exist
        if (!surveyRepository.existsById(surveyID)) {
            model.addAttribute("issue", "Survey does not exist");
            return "SurveyFillIssue";
        }

        Survey s = this.surveyRepository.findSurveyById(surveyID);

        // if survey is still being edited
        if (s.getStatus() == Survey.SurveyStatuses.BEING_EDITED) {
            model.addAttribute("issue", "Survey is being edited");
            return "SurveyFillIssue";
        }

        // if survey is still finished
        if (s.getStatus() == Survey.SurveyStatuses.FINISHED) {
            model.addAttribute("issue", "Survey is finished");
            return "SurveyFillIssue";
        }

        // for each question in survey add the response
        for (int index = 0; index < s.getQuestions().size(); index += 1) {
            Question q = s.getQuestions().get(index);
            addResponse(q, allParams.get("values" + q.getId()));
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

    private static String getPaginateHtml(Long surveyId, int page){
        @Language("html")
        final String paginateHtml = """
        <div hx-get="/survey/%d/answer/%d" hx-swap="afterend" hx-trigger="revealed"></div>
    """.formatted(surveyId,page);
        return paginateHtml;
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
            s += "<input type=\"radio\" id=\"" + option + "\" name=\"values" + q.getId() + "\" checked=\"checked\" value=\"" + option + "\">";
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
        s += "<input type=\"text\" id=\"values\" name=\"values" + q.getId() + "\"><br>";
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
        s += "<input type=\"range\" name=\"values" + q.getId() + "\" value=\"" + (q).getLow() + "\" min=\"" + (q).getLow() + "\" max=\"" + (q).getHigh() + "\" step=\"0.1\" oninput=\"this.nextElementSibling.value = this.value\">\n";
        s += "<output>" + (q).getLow() + "</output>";
        return s;
    }
}

