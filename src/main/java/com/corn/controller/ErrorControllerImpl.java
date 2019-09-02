package com.corn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;

import static com.corn.util.Constants.START;

/**
 * Custom error handling (replacement for the standard WhitePage)
 *
 * @author Oleg Zaidullin
 */
@RestController
public class ErrorControllerImpl implements ErrorController {

    private static final String PATH = "/error";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ErrorAttributes errorAttributes;

    public ErrorControllerImpl(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = PATH)
    public ErrorJson error(WebRequest webRequest, HttpServletResponse response) {
        logger.debug(START);
        return new ErrorJson(response.getStatus(), errorAttributes.getErrorAttributes(webRequest, false));
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

}
