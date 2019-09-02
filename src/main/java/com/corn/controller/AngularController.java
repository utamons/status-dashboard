package com.corn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

import static com.corn.util.Constants.START;
import static com.corn.util.Utils.isEmpty;
import static com.corn.util.Utils.isNotEmpty;


/**
 * This controller performs redirection to Angular frontend, otherwise standard SB endpoints will be used
 *
 * @author Oleg Zaidullin
 */
@Controller
public class AngularController {
    private final Logger         logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping({"/", "/home", "/history", "/report", "/subscribe", "/login", "/maintain"})
    public RedirectView index(HttpServletRequest request) {
        logger.debug(START);

        /*
         * Frontend is served as a static resource of backend, and frontend urls actually consider as backend urls
         * by Spring Boot, it's trying to find these urls in its controllers, and returns HTTP 404 to user.
         *
         * For handling frontend urls properly, we must catch and redirect all frontend urls to /index.html, which
         * is actually frontend code. Frontend code in turn, should perform internal navigation.
         *
         */

        String uri = request.getRequestURI().substring(1);
        String params = request.getQueryString();
        StringBuilder path = new StringBuilder("index.html");

        if (uri.length()>0 && isEmpty(params))
            path.append("?route=").append(uri);
        else if(uri.length()>0 && isNotEmpty(params))
            path.append("?route=").append(uri).append("&").append(params);
        else if (uri.length() == 0 && isNotEmpty(params))
            path.append("?").append(params);

        RedirectView redirectView = new RedirectView(path.toString());

        logger.debug("finish (successful)");
        return redirectView;
    }
}