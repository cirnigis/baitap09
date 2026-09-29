package vn.iostar.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductDTO {

	private Long id;

	@NotBlank(message = "Product name is required")
	@Size(max = 2000, message = "Product name is too long")
	private String name;

	private String description;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
	private BigDecimal price;

	private String imageUrl;

	@NotNull(message = "Please select a user")
	private Long userId;

	private String username;
}