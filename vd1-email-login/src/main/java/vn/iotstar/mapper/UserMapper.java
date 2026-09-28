package vn.iotstar.mapper;

import org.mapstruct.*;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target="roleName",source="role.name") UserDTO toDTO(User entity);
    @Mapping(target="role",ignore=true) @Mapping(target="password",ignore=true)
    @Mapping(target="id",ignore=true) @Mapping(target="createdAt",ignore=true) User toEntity(UserDTO dto);
    @Mapping(target="id",ignore=true) @Mapping(target="role",ignore=true) @Mapping(target="password",ignore=true)
    @Mapping(target="createdAt",ignore=true)
    void update(UserDTO dto,@MappingTarget User entity);
}
