package com.example.jwtexample.controller;

import javax.validation.Valid;

import com.example.jwtexample.dto.LoginDto;
import com.example.jwtexample.dto.TokenDto;
import com.example.jwtexample.jwt.JwtFilter;
import com.example.jwtexample.jwt.TokenProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 api
 */
@RestController
@RequestMapping("/api")
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    
    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {

        // username과 password를 받아서 Authentication Token을 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // 생성한 토큰을 이용해서 Authentication객체를 만들 때, CustomUserDetailsService의 loadUserByUsername 메소드가 실행 됨.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // 생성한 Authentication객체를 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증정보를 기반으로 토큰 생성
        String jwt = tokenProvider.createToken(authentication);

        //reponse헤더에 토큰 저장
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }
    
}
