package vn.iostar.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iostar.dto.RegisterDTO;
import vn.iostar.entity.Role;
import vn.iostar.entity.User;
import vn.iostar.repository.RoleRepository;
import vn.iostar.repository.UserRepository;
import vn.iostar.service.AuthService;
import vn.iostar.service.OtpService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final OtpService otpService;

	@Override
	@Transactional
	public void register(RegisterDTO dto) {

		if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}

		if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}

		if (!dto.getPassword().equals(dto.getConfirmPassword())) {

			throw new IllegalArgumentException("Password confirmation does not match");
		}

		Role role = roleRepository.findByNameIgnoreCase("ROLE_USER")
				.orElseThrow(() -> new IllegalStateException("ROLE_USER does not exist"));

		User user = User.builder().username(dto.getUsername()).email(dto.getEmail())
				.password(passwordEncoder.encode(dto.getPassword())).fullName(dto.getFullName()).enabled(false)
				.role(role).build();

		userRepository.save(user);

		otpService.sendRegisterOtp(dto.getEmail());
	}

	@Override
	@Transactional
	public boolean verifyRegister(String email, String otp) {

		boolean verified = otpService.verifyRegisterOtp(email, otp);

		if (!verified) {
			return false;
		}

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		user.setEnabled(true);

		return true;
	}

	@Override
	@Transactional
	public void forgotPassword(String email) {

		if (!userRepository.existsByEmailIgnoreCase(email)) {
			throw new IllegalArgumentException("Email does not exist");
		}

		otpService.sendResetPasswordOtp(email);
	}

	@Override
	public boolean verifyResetOtp(String email, String otp) {

		return otpService.verifyResetPasswordOtp(email, otp);
	}

	@Override
	@Transactional
	public void resetPassword(String email, String password) {

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("Email does not exist"));

		user.setPassword(passwordEncoder.encode(password));
	}
}