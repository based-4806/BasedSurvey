package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.*;
import com.based.basedsurvey.repo.SurveyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class ResultsSurveyController {
    private SurveyRepository surveyRepository;
    public final int NUM_BINS = 7; //arbitrary number for demonstration purposes

    @Autowired
    public ResultsSurveyController(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @GetMapping("/survey/{surveyID}/results")
    public String getResults(@PathVariable Long surveyID, Model model) {
        if (!surveyRepository.existsById(surveyID)) {
            model.addAttribute("issue", "Survey does not exist");
            return "SurveyResultsIssue";
        }

        model.addAttribute("surveyID", surveyID);
        Survey s = this.surveyRepository.findSurveyById(surveyID);
        model.addAttribute("surveyName", s.getName());

        // if survey has no questions
        if (s.getQuestions().isEmpty()) {
            model.addAttribute("issue", "Survey has no questions");
            return "SurveyResultsIssue";
        }

        // results will contain a dynamically generated string with all the responses
        String results = "";

        for (Question q : s.getQuestions()) {
            if (q instanceof MultiplechoiceQuestion) {
                results += getMultipleChoiceResults((MultiplechoiceQuestion) q);
            }
            else if (q instanceof OpenAnswerQuestion) {
                results += getOpenAnswerResults((OpenAnswerQuestion) q);
            }
            else if (q instanceof RangeQuestion) {
                results += getRangeResults((RangeQuestion) q);
            }
        }

        model.addAttribute("results", results);
        return "Results";
    }

    /**
     * Given a MultiplechoiceQuestion, return a String that represents all of its responses by % in html.
     * @param q MultiplechoiceQuestion
     * @return String of html elements
     */
    private String getMultipleChoiceResults(MultiplechoiceQuestion q) {
        String s = "";
        s += "<h3>" + q.getPrompt() + "</h3>";
        if (Objects.equals(q.getAdditionalInfo(), "")) ; else s += "additional notes: " + q.getAdditionalInfo() + "<br>";

        Map<Integer, Integer> responseCounts = new HashMap<>();
        //initialize counts for each option
        for (int i = 0; i < q.getOptions().size(); i++) {
            responseCounts.put(i, 0);
        }
        //count each response
        for (Integer response : q.getResponses()) {
            responseCounts.put(response, responseCounts.get(response) + 1);
        }

        for (int i = 0; i < q.getOptions().size(); i++) {
            s += "<p>" + q.getOptions().get(i) + ": ";
            if (q.getResponses().isEmpty()) {
                s += "0.00%</p>";
            }
            else {
                s += String.format("%.2f", ((100.0f * responseCounts.get(i)) / q.getResponses().size())) + "%</p>";
            }
        }
        s += "<br>";
        return s;
    }

    /**
     * Given an OpenAnswerQuestion, return a String that represents all of its responses in html.
     * @param q OpenAnswerQuestion
     * @return String of html elements
     */
    private String getOpenAnswerResults(OpenAnswerQuestion q) {
        String s = "";
        //doesn't quite work, but this should probably be done with css
        s += "<h3>" + q.getPrompt() + "</h3><div height='200px' overflow='scroll'>"; // could specify a class/id for the div
        if (Objects.equals(q.getAdditionalInfo(), "")) ; else s += "additional notes: " + q.getAdditionalInfo() + "<br>";
        if (q.getResponses().isEmpty()) {
            s += "<div>No responses.</div>";
        }
        else {
            for (String response : q.getResponses()) {
                s += "<div>" + response + "</div>";
            }
        }
        s += "</div><br>";

        return s;
    }

    /**
     * Given a RangeQuestion, return a String that represents all of its responses by a histogram in html.
     * @param q RangeQuestion
     * @return String of html elements
     */
    private String getRangeResults(RangeQuestion q) {
        String s = "";
        s += "<h3>" + q.getPrompt() + "</h3>";
        if (Objects.equals(q.getAdditionalInfo(), "")) ; else s += "additional notes: " + q.getAdditionalInfo() + "<br>";

        if (q.getResponses().isEmpty()) {
            s += "<p>No responses.</p>";
            return s;
        }
        s += "<table><tr><th>Bins\\Number of values:</th>";

        //TODO: set up for histogram better
        //don't really need to do below when using an actual library/doing math in js
        Float min = Collections.min(q.getResponses());
        float binWidth = getBinWidth(q.getResponses(), NUM_BINS);

        NavigableMap<Float, Integer> histogram = new TreeMap<>();
        //initialize bins with counts
        for (int i = 0; i < NUM_BINS; i++) {
            histogram.put(min + i * binWidth, 0);
        }

        //populate bins
        for (Float response : q.getResponses()) {
            histogram.put(histogram.floorKey(response), histogram.get(histogram.floorKey(response)) + 1);
        }
        int maxCount = Collections.max(histogram.values());
        //header, simply 1 for each count for demonstration purposes
        for (int i = 1; i <= maxCount; i++) {
            s += "<th>" + i + "</th>";
        }
        s += "</tr>";

        //populate table
        for (Float leftBound : histogram.keySet()) {
            s += "<tr><td>[" + String.format("%.2f", leftBound) + ", " + String.format("%.2f", leftBound + binWidth) + ")</td>";
            int i = 0;
            for (; i < histogram.get(leftBound); i++) {
                s += "<td>▬</td>"; // could specify a class for filled counts
            }
            for (; i < maxCount; i++) {
                s += "<td></td>"; // could specify a class for empty counts
            }
            s += "</tr>";
        }
        s += "</table><br>";

        return s;
    }

    /**
     * Gets the width of bins to use given a list of values to be binned and the number of desired bins
     * @param values List of values that will be binned
     * @param numBins
     * @return width of the bins
     */
    private float getBinWidth(List<Float> values, int numBins) {
        Float min = Collections.min(values);
        Float max = Collections.max(values);
        return (max - min) / numBins;
    }

}
