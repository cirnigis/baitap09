package vn.iostar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

	private Long id;

	@NotBlank(message = "Username is required")
	@Size(max = 50, message = "Username must not exceed 50 characters")
	private String username;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 150, message = "Email must not exceed 150 characters")
	private String email;

	private String fullName;
	
	private String images;

	private boolean enabled;

	private String roleName;

	private long productCount;
}