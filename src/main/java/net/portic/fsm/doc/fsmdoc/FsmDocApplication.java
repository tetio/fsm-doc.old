package net.portic.fsm.doc.fsmdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@PropertySource({ "classpath:application-${PORTIC_ENV}.properties" })
//public class FsmDocApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(FsmDocApplication.class, args);
//	}
//
//}
public class FsmDocApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(FsmDocApplication.class, args);
	}
}

