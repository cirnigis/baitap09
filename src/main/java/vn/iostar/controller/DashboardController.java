package vn.iostar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.iostar.service.ProductService;
import vn.iostar.service.UserService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	private final UserService userService;
	private final ProductService productService;

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("userCount", userService.countUsers());
		model.addAttribute("productCount", productService.countProducts());

		return "dashboard";
	}
}