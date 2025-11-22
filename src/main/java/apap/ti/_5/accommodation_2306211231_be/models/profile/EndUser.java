package apap.ti._5.accommodation_2306211231_be.models.profile;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
// removed UpdateTimestamp to control updatedAt manually

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class EndUser {

	@Id
	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "username", nullable = false, length = 100, unique = true)
	private String username;

	@Column(name = "password", nullable = false, length = 255)
	private String password;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "email", nullable = false, length = 255, unique = true)
	private String email;

	@Column(name = "gender", nullable = false)
	private Boolean gender; // true=male,false=female (adjust as needed)

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (isDeleted == null) {
			isDeleted = false;
		}
		// ensure updatedAt remains null on initial creation
		this.updatedAt = null;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
