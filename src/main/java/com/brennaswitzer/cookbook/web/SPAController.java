package com.brennaswitzer.cookbook.web;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class SPAController extends BasicErrorController {

    public SPAController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    public SPAController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        if (HttpStatus.NOT_FOUND.equals(status)) {
            response.setStatus(HttpStatus.OK.value());
            request.removeAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            request.removeAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE);
            request.removeAttribute(RequestDispatcher.ERROR_EXCEPTION);
            request.removeAttribute(RequestDispatcher.ERROR_MESSAGE);
            return new ModelAndView("forward:/");
        }
        return super.errorHtml(request, response);
    }

}
