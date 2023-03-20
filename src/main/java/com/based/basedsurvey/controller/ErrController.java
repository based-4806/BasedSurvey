package com.based.basedsurvey.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrController implements ErrorController {

    @RequestMapping("/error")
    public String errorPage(HttpServletRequest request, Model model){
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if(status == null){
            model.addAttribute("errorno", "unknown");
        }
        int statusCode = Integer.valueOf(status.toString());
        model.addAttribute("errorno", statusCode);
        return "error";
    }
}
