package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.*;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.service.*;
@Service @RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository products;
    private final UserRepository users;
    private final ProductMapper mapper;
    private final CloudinaryService images;
    private final CurrentAccount account;
    @Transactional(readOnly=true) public Page<ProductDTO> findAll(String keyword,int page,int size){
        return products.search(keyword==null?"":keyword.trim(),account.admin()?null:account.get().getId(),PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)),Sort.by(Sort.Direction.DESC,"id"))).map(mapper::toDTO);
    }
    private Product require(Long id){return products.findById(id).orElseThrow(()->new IllegalArgumentException("Sản phẩm không tồn tại."));}
    @Transactional(readOnly=true) public ProductDTO findById(Long id){var p=require(id);account.checkOwner(p.getUser().getId());return mapper.toDTO(p);}
    @Transactional public ProductDTO create(ProductDTO dto,MultipartFile file){
        Product p=mapper.toEntity(dto);p.setUser(users.findById(account.get().getId()).orElseThrow());
        replaceImage(p,file);return mapper.toDTO(products.save(p));
    }
    @Transactional public ProductDTO update(Long id,ProductDTO dto,MultipartFile file){
        var p=require(id);account.checkOwner(p.getUser().getId());mapper.update(dto,p);replaceImage(p,file);return mapper.toDTO(p);
    }
    private void replaceImage(Product p,MultipartFile file){
        if(file==null || file.isEmpty())return;
        String old=p.getImagePublicId();var uploaded=images.upload(file);
        try{images.delete(old);}catch(RuntimeException e){images.delete(uploaded.publicId());throw e;}
        p.setImageUrl(uploaded.url());p.setImagePublicId(uploaded.publicId());
    }
    @Transactional public void delete(Long id){var p=require(id);account.checkOwner(p.getUser().getId());images.delete(p.getImagePublicId());products.delete(p);}
    @Transactional(readOnly=true) public long countProducts(){return products.count();}
    @Transactional(readOnly=true) public long countByUser(Long id){return products.countByUserId(id);}
}
