package vn.iostar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vn.iostar.service.CloudinaryService;
import vn.iostar.service.CloudinaryUploadResult;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

	private final Cloudinary cloudinary;

	@Override
	public CloudinaryUploadResult upload(MultipartFile file) {

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Please select an image");
		}

		String type = file.getContentType();

		if (type == null || !type.startsWith("image/")) {
			throw new IllegalArgumentException("Only image files are allowed");
		}

		try {
			Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "shop/products"));

			return new CloudinaryUploadResult(String.valueOf(result.get("secure_url")),
					String.valueOf(result.get("public_id")));

		} catch (Exception e) {
			throw new IllegalStateException("Cloudinary upload failed", e);
		}
	}

	@Override
	public void delete(String publicId) {

		if (publicId == null || publicId.isBlank()) {
			return;
		}

		try {
			cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));

		} catch (Exception e) {
			throw new IllegalStateException("Cloudinary image deletion failed", e);
		}
	}
}