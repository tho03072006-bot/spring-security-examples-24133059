package vn.iotstar.mapper;

import org.mapstruct.*;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface ProductMapper {
    @Mapping(target="userId",source="user.id") @Mapping(target="username",source="user.username") ProductDTO toDTO(Product entity);
    @Mapping(target="id",ignore=true) @Mapping(target="user",ignore=true) @Mapping(target="imageUrl",ignore=true) @Mapping(target="imagePublicId",ignore=true) @Mapping(target="createdAt",ignore=true) Product toEntity(ProductDTO dto);
    @Mapping(target="id",ignore=true) @Mapping(target="user",ignore=true) @Mapping(target="imageUrl",ignore=true) @Mapping(target="imagePublicId",ignore=true) @Mapping(target="createdAt",ignore=true) void update(ProductDTO dto,@MappingTarget Product entity);
}
