package com.jeonlog.exhibition_recommender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ExhibitionRecommenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExhibitionRecommenderApplication.class, args);
	}

}
