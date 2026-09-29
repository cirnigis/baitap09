package vn.iostar.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import vn.iostar.dto.UserDTO;

public interface UserService {

	Page<UserDTO> findAll(String keyword, int page, int size);

	UserDTO findById(Long id);

	UserDTO create(UserDTO dto);

	UserDTO update(Long id, UserDTO dto);

	void delete(Long id);

	long countUsers();

	long countProducts(Long userId);

	UserDTO findProfile(Long userId);

	UserDTO updateProfile(Long userId, UserDTO dto, MultipartFile image);

	void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);
}