package com.example.jwtexample.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.example.jwtexample.entity.User;
import com.example.jwtexample.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // DB에서 유저의 정보를 가져와서 리턴해주는 작업
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService { 
   
   @Autowired
   private UserRepository userRepository;

   @Override
   @Transactional
   public UserDetails loadUserByUsername(final String username) {
      return userRepository.findOneWithAuthoritiesByUsername(username)
         .map(user -> createUser(username, user))
         .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
   }

   private org.springframework.security.core.userdetails.User createUser(String username, User user) {
      if (!user.isActivated()) {
         throw new RuntimeException(username + " -> 활성화되어 있지 않습니다.");
      }
      List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
              .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
              .collect(Collectors.toList());
      return new org.springframework.security.core.userdetails.User(user.getUsername(),
              user.getPassword(),
              grantedAuthorities);
   }
}
