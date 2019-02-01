package net.portic.fsm.doc.fsmdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FsmDocApplication {

	public static void main(String[] args) {
		SpringApplication.run(FsmDocApplication.class, args);
	}

}

