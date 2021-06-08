package com.example.jwtexample.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@EnableWebSecurity //기본적인 Web보안을 활성화 하겠다.
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    @Override 
    protected void configure(HttpSecurity http)throws Exception {
        http.authorizeRequests() //http servlet request 를 사용하는 요청에 대해서 접근 제한을 하겠다.
            .antMatchers("/api/hello").permitAll()
            .anyRequest().authenticated(); //나머지 요청들은 모두 인증이 필요하다.
    }


    
}
