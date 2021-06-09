package com.example.jwtexample.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Optional;

import com.example.jwtexample.dto.UserDto;
import com.example.jwtexample.entity.Authority;
import com.example.jwtexample.entity.User;
import com.example.jwtexample.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    //회원가입.
    @Transactional
    public User signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        //가입이 되어 있지 않을 경우 권한정보를 만들고.
        Authority authority = Authority.builder()
                                        .authorityName("ROLE_USER")
                                        .build();

        User user = User.builder()
                        .username(userDto.getUsername())
                        .password(passwordEncoder.encode(userDto.getPassword()))
                        .nickname(userDto.getNickname())
                        .authorities(Collections.singleton(authority))
                        .activated(true)
                        .build();

        // DB에 저장
        return userRepository.save(user);
    }

    // username에 해당하는 User객체와 권한 정보를 가져옴
    @Transactional(readOnly = true) 
    public Optional<User> 
    getUserWithAuthorities(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }

    // 현재 security context에 들어있는 username에 해당하는 User객체와 권한 정보를 가져옴
    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 
       if (authentication == null) {
          log.info("Security Context에 인증 정보가 없습니다.");
          return Optional.empty();
       }
 
       // authentication 객체에서 username을 리턴해준다.
       String username = null;
       if (authentication.getPrincipal() instanceof UserDetails) {
          UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
          username = springSecurityUser.getUsername();
       } else if (authentication.getPrincipal() instanceof String) {
          username = (String) authentication.getPrincipal();
       }
 
       return Optional.ofNullable(username).flatMap(userRepository::findOneWithAuthoritiesByUsername);
    }

    public static Optional<String> getCurrentUsername() {
        // Security Context에서 authentication를 꺼낸다.
       final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 
       if (authentication == null) {
          log.info("Security Context에 인증 정보가 없습니다.");
          return Optional.empty();
       }
 
       // authentication 객체에서 username을 리턴해준다.
       String username = null;
       if (authentication.getPrincipal() instanceof UserDetails) {
          UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
          username = springSecurityUser.getUsername();
       } else if (authentication.getPrincipal() instanceof String) {
          username = (String) authentication.getPrincipal();
       }
 
       return Optional.ofNullable(username);
    }
}