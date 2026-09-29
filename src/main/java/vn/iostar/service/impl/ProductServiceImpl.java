package vn.iostar.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.iostar.dto.ProductDTO;
import vn.iostar.entity.Product;
import vn.iostar.entity.User;
import vn.iostar.mapper.ProductMapper;
import vn.iostar.repository.ProductRepository;
import vn.iostar.repository.UserRepository;
import vn.iostar.service.CloudinaryService;
import vn.iostar.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final ProductMapper mapper;
	private final CloudinaryService cloudinaryService;

	@Override
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAll(String keyword, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<Product> result;

		if (keyword == null || keyword.isBlank()) {
			result = productRepository.findAll(pageable);
		} else {
			result = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword,
					pageable);
		}

		return result.map(mapper::toDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		return mapper.toDTO(product);
	}

	@Override
	@Transactional
	public ProductDTO create(ProductDTO dto, MultipartFile image) {

		User user = userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Product product = mapper.toEntity(dto);

		product.setUser(user);

		if (image != null && !image.isEmpty()) {

			var upload = cloudinaryService.upload(image);

			product.setImageUrl(upload.url() + "|" + upload.publicId());
		}

		Product saved = productRepository.save(product);

		return mapper.toDTO(saved);
	}

	@Override
	@Transactional
	public ProductDTO update(Long id, ProductDTO dto, MultipartFile image) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		User user = userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		product.setName(dto.getName());
		product.setDescription(dto.getDescription());
		product.setPrice(dto.getPrice());
		product.setUser(user);

		if (image != null && !image.isEmpty()) {

			deleteOldImage(product.getImageUrl());

			var upload = cloudinaryService.upload(image);

			product.setImageUrl(upload.url() + "|" + upload.publicId());
		}

		Product saved = productRepository.save(product);

		return mapper.toDTO(saved);
	}

	@Override
	@Transactional
	public void delete(Long id) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		deleteOldImage(product.getImageUrl());

		productRepository.delete(product);
	}

	private void deleteOldImage(String imageUrl) {

		if (imageUrl == null || imageUrl.isBlank()) {
			return;
		}

		int separator = imageUrl.indexOf('|');

		if (separator < 0) {
			return;
		}

		String publicId = imageUrl.substring(separator + 1);

		cloudinaryService.delete(publicId);
	}

	@Override
	@Transactional(readOnly = true)
	public long countProducts() {
		return productRepository.count();
	}

	@Override
	@Transactional(readOnly = true)
	public long countByUser(Long userId) {
		return productRepository.countByUserId(userId);
	}
}