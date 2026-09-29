package vn.iostar.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.iostar.dto.ProductDTO;

public interface ProductService {

	Page<ProductDTO> findAll(String keyword, int page, int size);

	ProductDTO findById(Long id);

	ProductDTO create(ProductDTO dto, MultipartFile image);

	ProductDTO update(Long id, ProductDTO dto, MultipartFile image);

	void delete(Long id);

	long countProducts();

	long countByUser(Long userId);
}