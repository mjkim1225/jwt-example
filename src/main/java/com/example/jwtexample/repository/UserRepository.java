package com.example.jwtexample.repository;

import java.util.Optional;

import com.example.jwtexample.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{

    @EntityGraph(attributePaths = "authorities") //쿼리가 수행될 때, Lazy조회가 아니라 Eager조회로 authorities정보를 같이 가져온다
    Optional<User> findOneWithAuthoritiesByUsername(String username);
    
}
