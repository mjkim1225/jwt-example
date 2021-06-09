package com.example.jwtexample.config;

import com.example.jwtexample.jwt.JwtAccessDeniedHandler;
import com.example.jwtexample.jwt.JwtAuthenticationEntryPoint;
import com.example.jwtexample.jwt.JwtSecurityConfig;
import com.example.jwtexample.jwt.TokenProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity //기본적인 Web보안을 활성화 하겠다.
@EnableGlobalMethodSecurity(prePostEnabled = true) //나중에 @preAuthorized 어노테이션을 메소드단위로 사용하기 위해 적용
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(TokenProvider tokenProvider, CorsFilter corsFilter,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {

        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder PasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(
                        "/favicon.ico",
                        "/error"
                );
    }


    @Override 
    protected void configure(HttpSecurity http)throws Exception {
        http
            // token을 사용하는 방식이기 때문에 csrf를 disable
            .csrf().disable()

            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

            .exceptionHandling() //예외에 대한 핸들러
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            .and()
            .headers()
            .frameOptions()
            .sameOrigin()

            // 세션을 사용하지 않기 때문에 STATELESS로 설정
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests() //http servlet request 를 사용하는 요청에 대한 접근 제한
            .antMatchers("/api/hello").permitAll()
            .antMatchers("/api/authenticate").permitAll() //토큰을 받기위한 log in api -- 토큰 없는 상태로 요청이 들어옴
            .antMatchers("/api/signup").permitAll() //회원 가입을 위한 api -- 토큰 없는 상태로 요청이 들어옴
            // 토큰이 없는 상테에서 들어오는 요청은 모두 허락함
            .anyRequest().authenticated()

            .and()
            .apply(new JwtSecurityConfig(tokenProvider)); // jwtFilter를 addFilterBefore로 등록했던 JwtSecurityConfig도 적용
    }


    
}
