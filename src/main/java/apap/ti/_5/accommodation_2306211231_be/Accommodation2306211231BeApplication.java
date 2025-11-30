package apap.ti._5.accommodation_2306211231_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManagerFactory;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class Accommodation2306211231BeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Accommodation2306211231BeApplication.class, args);
	}

	@Bean
	CommandLineRunner listManagedEntities(Optional<EntityManagerFactory> emfOpt) {
		return args -> {
			if (emfOpt.isEmpty()) {
				return;
			}
			try {
				var emf = emfOpt.get();
				var metamodel = emf.getMetamodel();
				if (metamodel == null) return;
				System.out.println("==== Managed JPA Entities ====");
				metamodel.getEntities().forEach(e -> System.out.println(" - " + e.getName()));
				System.out.println("===============================");
			} catch (Exception ex) {
				// Defensive: in test slices or when EMF is a mock, metamodel may be null or throw.
				// Avoid failing application startup because of diagnostic printing.
			}
		};
	}

}
