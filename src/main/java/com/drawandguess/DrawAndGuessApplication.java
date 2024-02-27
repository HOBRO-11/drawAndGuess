package com.drawandguess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@ComponentScan(excludeFilters = { @Filter(type = FilterType.ANNOTATION, classes = { Component.class, Repository.class,
		Service.class, Controller.class }) })
public class DrawAndGuessApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrawAndGuessApplication.class, args);
	}

}
