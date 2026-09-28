package vn.iotstar.service;

public interface UserService {
org.springframework.data.domain.Page<vn.iotstar.dto.UserDTO> findAll(String keyword,int page,int size);
vn.iotstar.dto.UserDTO findById(Long id);
vn.iotstar.dto.UserDTO create(vn.iotstar.dto.UserDTO dto);
vn.iotstar.dto.UserDTO update(Long id,vn.iotstar.dto.UserDTO dto);
void delete(Long id); long countUsers(); long countProducts(Long userId);
}
