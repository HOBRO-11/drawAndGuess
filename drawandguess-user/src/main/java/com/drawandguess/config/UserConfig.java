package com.drawandguess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.drawandguess.repository.UserQueryRepository;
import com.drawandguess.repository.UserQueryRepositoryImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Configuration
public class UserConfig {

    @PersistenceContext
    EntityManager em;

    @Bean
    public UserQueryRepository userQueryRepository(EntityManager em) {
        return new UserQueryRepositoryImpl(em);
    }
}
