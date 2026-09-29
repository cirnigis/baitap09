package vn.iostar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {

	@NotBlank(message = "Username or email is required")
	private String username;

	@NotBlank(message = "Password is required")
	private String password;
}