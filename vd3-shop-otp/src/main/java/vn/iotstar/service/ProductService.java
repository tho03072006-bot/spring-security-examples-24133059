package vn.iotstar.service;

public interface ProductService {
org.springframework.data.domain.Page<vn.iotstar.dto.ProductDTO> findAll(String keyword,int page,int size);
vn.iotstar.dto.ProductDTO findById(Long id);
vn.iotstar.dto.ProductDTO create(vn.iotstar.dto.ProductDTO dto,org.springframework.web.multipart.MultipartFile image);
vn.iotstar.dto.ProductDTO update(Long id,vn.iotstar.dto.ProductDTO dto,org.springframework.web.multipart.MultipartFile image);
void delete(Long id); long countProducts(); long countByUser(Long userId);
}
