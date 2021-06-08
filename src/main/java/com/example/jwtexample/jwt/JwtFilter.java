package com.example.jwtexample.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtFilter extends GenericFilter {

    public static final String AUTHORIZATION_HEADER = "Authorizaion";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    //jwt token의 인증정보를 실행 중인 SecurityContext에 저장하는 역할 수행
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = resolveToken(httpServletRequest); //request 에서 토큰을 받아서
        String requestURI = httpServletRequest.getRequestURI();

        if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) { // 유효성 검사를 한다.
            Authentication authentication = tokenProvider.getAuthentication(jwt); //token에서 authenticaion 객체를 받아
            SecurityContextHolder.getContext().setAuthentication(authentication); //SecurityContext에 set해준다.
            log.info("Security Context에 '{}' 인증 정보를 저장했습니다. uri: {}", authentication.getName(), requestURI);
        }else {
            log.info("유효한 jwt 토큰이 없습니다. uri: {}", requestURI);
        }
    }

    // request header에서 token 정보를 꺼내옴
    private String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
