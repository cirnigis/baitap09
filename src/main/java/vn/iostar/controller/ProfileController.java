package vn.iostar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iostar.dto.ChangePasswordDTO;
import vn.iostar.dto.UserDTO;
import vn.iostar.security.CustomUserDetails;
import vn.iostar.service.UserService;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

	private final UserService userService;

	@GetMapping
	public String profile(Authentication authentication, Model model) {

		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

		model.addAttribute("userDTO", userService.findProfile(user.getId()));

		model.addAttribute("changePasswordDTO", new ChangePasswordDTO());

		return "profile/index";
	}

	@PostMapping("/update")
	public String updateProfile(Authentication authentication, @Valid @ModelAttribute("userDTO") UserDTO dto,
			BindingResult result, @RequestParam(value = "image", required = false) MultipartFile image, Model model,
			RedirectAttributes redirect) {

		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

		if (result.hasErrors()) {

			model.addAttribute("changePasswordDTO", new ChangePasswordDTO());

			return "profile/index";
		}

		try {

			userService.updateProfile(user.getId(), dto, image);

			redirect.addFlashAttribute("success", "Profile updated successfully.");

			return "redirect:/profile";

		} catch (IllegalArgumentException e) {

			result.reject("profile.error", e.getMessage());

			model.addAttribute("changePasswordDTO", new ChangePasswordDTO());

			return "profile/index";
		}
	}

	@PostMapping("/change-password")
	public String changePassword(Authentication authentication,
			@Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto, BindingResult result, Model model,
			RedirectAttributes redirect) {

		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

		if (result.hasErrors()) {

			model.addAttribute("userDTO", userService.findProfile(user.getId()));

			return "profile/index";
		}

		try {

			userService.changePassword(user.getId(), dto.getCurrentPassword(), dto.getNewPassword(),
					dto.getConfirmPassword());

			redirect.addFlashAttribute("success", "Password changed successfully.");

			return "redirect:/profile";

		} catch (IllegalArgumentException e) {

			result.reject("password.error", e.getMessage());

			model.addAttribute("userDTO", userService.findProfile(user.getId()));

			return "profile/index";
		}
	}
}