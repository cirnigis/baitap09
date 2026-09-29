package vn.iostar.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.iostar.dto.UserDTO;
import vn.iostar.entity.Role;
import vn.iostar.entity.User;
import vn.iostar.mapper.UserMapper;
import vn.iostar.repository.ProductRepository;
import vn.iostar.repository.RoleRepository;
import vn.iostar.repository.UserRepository;
import vn.iostar.service.CloudinaryService;
import vn.iostar.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final ProductRepository productRepository;
	private final UserMapper mapper;
	private final PasswordEncoder passwordEncoder;
	private final CloudinaryService cloudinaryService;

	@Override
	@Transactional(readOnly = true)
	public Page<UserDTO> findAll(String keyword, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<User> result;

		if (keyword == null || keyword.isBlank()) {
			result = userRepository.findAll(pageable);
		} else {
			result = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword, keyword,
					pageable);
		}

		return result.map(user -> {

			UserDTO dto = mapper.toDTO(user);

			dto.setProductCount(productRepository.countByUserId(user.getId()));

			return dto;
		});
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {

		User user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

		UserDTO dto = mapper.toDTO(user);

		dto.setProductCount(productRepository.countByUserId(user.getId()));

		return dto;
	}

	@Override
	@Transactional
	public UserDTO create(UserDTO dto) {

		if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}

		if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}

		String roleName = dto.getRoleName();

		if (roleName == null || roleName.isBlank()) {
			roleName = "ROLE_USER";
		} else if (!roleName.startsWith("ROLE_")) {
			roleName = "ROLE_" + roleName;
		}

		Role role = roleRepository.findByNameIgnoreCase(roleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found"));

		User user = mapper.toEntity(dto);

		user.setRole(role);

		// Default password for users created by Admin
		user.setPassword(passwordEncoder.encode("123456"));

		user.setEnabled(dto.isEnabled());

		User saved = userRepository.save(user);

		UserDTO result = mapper.toDTO(saved);

		result.setProductCount(0);

		return result;
	}

	@Override
	@Transactional
	public UserDTO update(Long id, UserDTO dto) {

		User user = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

		if (!user.getUsername().equalsIgnoreCase(dto.getUsername())
				&& userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {

			throw new IllegalArgumentException("Username already exists");
		}

		if (!user.getEmail().equalsIgnoreCase(dto.getEmail())
				&& userRepository.existsByEmailIgnoreCase(dto.getEmail())) {

			throw new IllegalArgumentException("Email already exists");
		}

		String roleName = dto.getRoleName();

		if (roleName == null || roleName.isBlank()) {
			roleName = "ROLE_USER";
		} else if (!roleName.startsWith("ROLE_")) {
			roleName = "ROLE_" + roleName;
		}

		Role role = roleRepository.findByNameIgnoreCase(roleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found"));

		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setFullName(dto.getFullName());
		user.setEnabled(dto.isEnabled());
		user.setRole(role);

		User saved = userRepository.save(user);

		UserDTO result = mapper.toDTO(saved);

		result.setProductCount(productRepository.countByUserId(saved.getId()));

		return result;
	}

	@Override
	@Transactional
	public void delete(Long id) {

		if (!userRepository.existsById(id)) {
			throw new IllegalArgumentException("User not found with id: " + id);
		}

		userRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public long countUsers() {
		return userRepository.count();
	}

	@Override
	@Transactional(readOnly = true)
	public long countProducts(Long userId) {
		return productRepository.countByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO findProfile(Long userId) {

		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

		UserDTO dto = mapper.toDTO(user);

		dto.setProductCount(productRepository.countByUserId(userId));

		return dto;
	}

	@Override
	@Transactional
	public UserDTO updateProfile(Long userId, UserDTO dto, MultipartFile image) {

		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (!user.getUsername().equalsIgnoreCase(dto.getUsername())
				&& userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {

			throw new IllegalArgumentException("Username already exists");
		}

		if (!user.getEmail().equalsIgnoreCase(dto.getEmail())
				&& userRepository.existsByEmailIgnoreCase(dto.getEmail())) {

			throw new IllegalArgumentException("Email already exists");
		}

		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		user.setFullName(dto.getFullName());

		if (image != null && !image.isEmpty()) {

			deleteOldUserImage(user.getImages());

			var upload = cloudinaryService.upload(image);

			user.setImages(upload.url() + "|" + upload.publicId());
		}

		User saved = userRepository.save(user);

		UserDTO result = mapper.toDTO(saved);

		result.setProductCount(productRepository.countByUserId(saved.getId()));

		return result;
	}

	private void deleteOldUserImage(String imageUrl) {

		if (imageUrl == null || imageUrl.isBlank()) {
			return;
		}

		int separator = imageUrl.indexOf('|');

		if (separator < 0) {
			return;
		}

		String publicId = imageUrl.substring(separator + 1);

		if (!publicId.isBlank()) {
			cloudinaryService.delete(publicId);
		}
	}

	@Override
	@Transactional
	public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {

		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {

			throw new IllegalArgumentException("Current password is incorrect");
		}

		if (!newPassword.equals(confirmPassword)) {

			throw new IllegalArgumentException("New password confirmation does not match");
		}

		if (passwordEncoder.matches(newPassword, user.getPassword())) {

			throw new IllegalArgumentException("New password must be different from current password");
		}

		user.setPassword(passwordEncoder.encode(newPassword));

		userRepository.save(user);
	}
}