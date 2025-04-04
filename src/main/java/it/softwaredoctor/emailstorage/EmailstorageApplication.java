package it.softwaredoctor.emailstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EmailstorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailstorageApplication.class, args);
	}

}
