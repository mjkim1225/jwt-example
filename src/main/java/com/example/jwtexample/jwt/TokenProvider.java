package com.example.jwtexample.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;


@Slf4j
@Component
public class TokenProvider implements InitializingBean{

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.token-validity-in-seconds}")
    private long tokenValidityInMilliseconds ;
    
    private static final String AUTHORITIES_KEY = "auth";
    private Key key;
    
    @Override //빈이 생성이 되고 주입을 받은 후에 secret값을 디코딩해서 key변수에 할당하기 위함
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);   
        this.key = Keys.hmacShaKeyFor(keyBytes);     
    }


    // jwt 토큰을 생성함
    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);
    
        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities) //JWT는 claim방식의 토큰을 사용하는데 claim이란 사용자에 대한 속성 등을 의미 
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }
    

    // 토근을 받아서 토큰에 담겨있는 권한정보를 이영해 Authentication 객체 던져줌
    public Authentication getAuthentication(String token) {

        // 1. 토큰으로 claims을 생성
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        // 2. claims에서 권한정보를 빼낸다.
        Collection<? extends GrantedAuthority> authorities 
            = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        
        // 3. 권한 정보를 빼네서 유저 객체를 만들어준다.
        User principal = new User(claims.getSubject(), "", authorities); //entity.user가 아님 주의

        // 4. 유저 객체, 토큰, 권한정보를 이용해서 authentication 객체 리턴
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰의 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
         } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
         } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
         } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
         }
         return false;
    }
    
}
