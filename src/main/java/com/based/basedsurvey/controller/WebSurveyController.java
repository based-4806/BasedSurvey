package com.based.basedsurvey.controller;

import com.based.basedsurvey.data.Survey;
import com.based.basedsurvey.repo.QuestionRepository;
import com.based.basedsurvey.repo.SurveyRepository;
import lombok.extern.java.Log;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log
@Controller
public class WebSurveyController {
    final int pageSize = 10;

    SurveyRepository surveyRepository;
    QuestionRepository questionRepository;
    @Autowired
    public WebSurveyController(SurveyRepository surveyRepository, QuestionRepository questionRepository){
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping(path = "/")
    public String homePage() {
        return "index";
    }

    @PostMapping("survey/create")
    @ResponseBody
    public String createSurveys(@RequestParam String name) {
        Survey survey = new Survey(name);
        surveyRepository.save(survey);
        return "<tr >" + formatBeingEditedSurvey(survey, true);
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

    @GetMapping("/surveys/{page}")
    @ResponseBody
    public String getSurveysHtmxInfiniteLoad(@PathVariable int page){
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        List<Survey> surveys = surveyRepository.findAll(pageRequest).getContent();

        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Survey survey: surveys) {
            if(i == pageSize - 1)
                result.append(paginatedRowHtml.formatted(page + 1));
            else
                result.append("<tr>");

            if (survey.getStatus() == Survey.SurveyStatuses.BEING_FILLED)
                result.append(formatBeingFilledSurvey(survey));
            else if(survey.getStatus() == Survey.SurveyStatuses.BEING_EDITED)
                result.append(formatBeingEditedSurvey(survey));
            else
                result.append(formatFinishedSurvey(survey));
            i++;
        }

        return result.toString();
    }

    @Language("html")
    final String surveyBeingFilledHtml = """
                <td>%d</td>
                <td><p>%s<p/></td>
                <td><a href="/survey/%d/answer">Link</a></td>
                <td>
                    <button hx-confirm="Are you sure you want to delete this based survey?" hx-target="closest tr" hx-swap="outerHTML" hx-trigger="click" hx-delete="/survey/%d">
                    Delete
                    </button>
                </td>
                <td>Survey cannot be edited</td>
                <td>                   
                    <form method="get" action="/survey/%d/results" class="inline">
                        <button type="submit" name="submit_param" value="submit_value" class="link-button">
                            O
                        </button>
                    </form>
                </td>
                <td>
                    <form method="post" action="/survey/finishSurvey">
                        <input type="hidden" name="id" value="%d">
                        <button type="submit">Finish</button>
                    </form>
                </td>
                <td>BEING FILLED</td>
            </tr>
            """;

    @Language("html")
    final String surveyBeingEditedHtml = """
                <td>%d</td>
                <td>                 
                    <span style="display: inline-flex;align-items: center;" hx-target="this" hx-swap="outerHTML">
                        <h2>%s </h2>
                        <button hx-get="/survey/htmx/rename/%d">Rename</button>
                    </span></td>
                <td><p>---</p></td>
                <td>
                    <button hx-confirm="Are you sure you want to delete this based survey?" hx-target="closest tr" hx-swap="outerHTML" hx-trigger="click" hx-delete="/survey/%d">
                    Delete
                    </button>
                </td>
                <td>                        
                    <form method="get" action="/survey/%d/edit" class="inline">
                        <input type="hidden" name="id" value="%d">
                        <button type="submit" name="submit_param" value="submit_value" class="link-button">
                            O
                        </button>
                    </form>
                </td>
                <td>                   
                    <form method="get" action="/survey/%d/results" class="inline">
                        <button type="submit" name="submit_param" value="submit_value" class="link-button">
                            O
                        </button>
                    </form>
                </td>
                <td>
                    <form method="post" action="/survey/finishSurvey">
                        <input type="hidden" name="id" value="%d">
                        <button type="submit">Finish</button>
                    </form>
                </td>
                <td>BEING EDITED</td>
            </tr>
            """;

    @Language("html")
    final String surveyFinishedHtml = """
                <td>%d</td>
                <td><p>%s<p/></td>
                <td>Survey cannot be filled</td>
                <td>
                    <button hx-confirm="Are you sure you want to delete this based survey?" hx-target="closest tr" hx-swap="outerHTML" hx-trigger="click" hx-delete="/survey/%d">
                    Delete
                    </button>
                </td>
                <td>Survey cannot be edited</td>
                <td>                   
                    <form method="get" action="/survey/%d/results" class="inline">
                        <button type="submit" name="submit_param" value="submit_value" class="link-button">
                            O
                        </button>
                    </form>
                </td>
                <td>Survey finished</td>
                <td>FINISHED</td>
            </tr>
            """;

    @Language("html")
    final String paginatedRowHtml = """
        <tr hx-swap="afterend" hx-trigger="revealed" hx-get="surveys/%d">
    """;

    private String formatBeingEditedSurvey(Survey survey){
        return surveyBeingEditedHtml.formatted(survey.getId(), survey.getName(), survey.getId(), survey.getId(), survey.getId(), survey.getId(), survey.getId(), survey.getId());
    }

    private String formatBeingEditedSurvey(Survey survey, boolean newSurvey){
        if(newSurvey) return surveyBeingEditedHtml.formatted(survey.getId(), survey.getName() + "<span class='newSurvey'> new! </span>", survey.getId(), survey.getId(), survey.getId(), survey.getId(), survey.getId(), survey.getId());
        else return formatBeingEditedSurvey(survey);
    }

    private String formatBeingFilledSurvey(Survey survey){
        return surveyBeingFilledHtml.formatted(survey.getId(), survey.getName(), survey.getId(), survey.getId(), survey.getId(), survey.getId());
    }

    private String formatFinishedSurvey(Survey survey){
        return surveyFinishedHtml.formatted(survey.getId(), survey.getName(), survey.getId(), survey.getId());
    }
}
