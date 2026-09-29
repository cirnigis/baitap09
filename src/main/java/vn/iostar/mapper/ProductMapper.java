package vn.iostar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import vn.iostar.dto.ProductDTO;
import vn.iostar.entity.Product;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "username", source = "user.username")
	ProductDTO toDTO(Product entity);

	@Mapping(target = "user", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	Product toEntity(ProductDTO dto);
}