package vn.iostar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iostar.dto.ProductDTO;
import vn.iostar.service.ProductService;
import vn.iostar.service.UserService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final UserService userService;

	@GetMapping
	public String list(@RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, Model model) {

		Page<ProductDTO> products = productService.findAll(keyword, page, size);

		model.addAttribute("products", products);
		model.addAttribute("keyword", keyword);
		model.addAttribute("size", size);

		return "products/list";
	}

	@GetMapping("/create")
	public String createForm(Model model) {

		model.addAttribute("productDTO", new ProductDTO());
		model.addAttribute("users", userService.findAll("", 0, 100).getContent());
		model.addAttribute("mode", "create");

		return "products/form";
	}

	@PostMapping("/create")
	public String create(@Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result,
			@RequestParam(value = "image", required = false) MultipartFile image, Model model,
			RedirectAttributes redirect) {

		System.out.println("========== CREATE PRODUCT ==========");
		System.out.println("Name: " + dto.getName());
		System.out.println("Description: " + dto.getDescription());
		System.out.println("Price: " + dto.getPrice());
		System.out.println("User ID: " + dto.getUserId());

		if (image != null) {
			System.out.println("Image name: " + image.getOriginalFilename());
			System.out.println("Image size: " + image.getSize());
			System.out.println("Image type: " + image.getContentType());
		} else {
			System.out.println("Image is NULL");
		}

		if (result.hasErrors()) {

			System.out.println("VALIDATION ERRORS:");

			result.getAllErrors().forEach(error -> System.out.println(error.toString()));

			model.addAttribute("users", userService.findAll("", 0, 100).getContent());

			model.addAttribute("mode", "create");

			return "products/form";
		}

		try {

			System.out.println("Calling productService.create()...");

			productService.create(dto, image);

			System.out.println("Product created successfully.");

			redirect.addFlashAttribute("success", "Product created successfully.");

			return "redirect:/products";

		} catch (Exception e) {

			System.out.println("CREATE PRODUCT ERROR:");
			e.printStackTrace();

			result.reject("product.error", "Could not create product: " + e.getMessage());

			model.addAttribute("users", userService.findAll("", 0, 100).getContent());

			model.addAttribute("mode", "create");

			return "products/form";
		}
	}

	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable Long id, Model model) {

		model.addAttribute("productDTO", productService.findById(id));

		model.addAttribute("users", userService.findAll("", 0, 100).getContent());

		model.addAttribute("mode", "edit");

		return "products/form";
	}

	@PostMapping("/edit/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("productDTO") ProductDTO dto,
			BindingResult result, @RequestParam(value = "image", required = false) MultipartFile image, Model model,
			RedirectAttributes redirect) {

		if (result.hasErrors()) {

			model.addAttribute("users", userService.findAll("", 0, 100).getContent());

			model.addAttribute("mode", "edit");

			return "products/form";
		}

		try {

			productService.update(id, dto, image);

			redirect.addFlashAttribute("success", "Product updated successfully.");

			return "redirect:/products";

		} catch (IllegalArgumentException e) {

			result.reject("product.error", e.getMessage());

			model.addAttribute("users", userService.findAll("", 0, 100).getContent());

			model.addAttribute("mode", "edit");

			return "products/form";
		}
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirect) {

		productService.delete(id);

		redirect.addFlashAttribute("success", "Product deleted successfully.");

		return "redirect:/products";
	}
}