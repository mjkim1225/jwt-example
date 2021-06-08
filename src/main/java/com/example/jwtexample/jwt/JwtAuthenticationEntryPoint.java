package com.example.jwtexample.jwt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint{

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException auth)
            throws IOException, ServletException {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED); //401 error
    }
    

}