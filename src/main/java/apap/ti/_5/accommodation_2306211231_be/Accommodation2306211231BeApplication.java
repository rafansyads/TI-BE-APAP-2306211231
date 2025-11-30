package apap.ti._5.accommodation_2306211231_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class Accommodation2306211231BeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Accommodation2306211231BeApplication.class, args);
	}

	@Bean
	CommandLineRunner listManagedEntities(EntityManagerFactory emf) {
		return args -> {
			System.out.println("==== Managed JPA Entities ====");
			emf.getMetamodel().getEntities().forEach(e -> System.out.println(" - " + e.getName()));
			System.out.println("===============================");
		};
	}

}
