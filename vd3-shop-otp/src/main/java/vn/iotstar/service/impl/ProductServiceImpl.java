package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService imageService;
    private final ImageCleanup imageCleanup;
    private final CurrentAccount currentAccount;

    // Admin thấy mọi sản phẩm; user chỉ thấy sản phẩm của mình
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, int page, int size) {
        Long ownerId = currentAccount.isAdmin() ? null : currentAccount.get().getId();
        String text = keyword == null ? "" : keyword.trim();
        return productRepository.search(text, ownerId, pageRequest(page, size)).map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = requireOwnedProduct(id);
        return productMapper.toDTO(product);
    }

    // Sản phẩm mới luôn thuộc user đang đăng nhập, không lấy userId từ form
    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto, MultipartFile image) {
        Product product = productMapper.toEntity(dto);
        product.setUser(userRepository.findById(currentAccount.get().getId()).orElseThrow());
        replaceImage(product, image);
        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto, MultipartFile image) {
        Product product = requireOwnedProduct(id);
        productMapper.update(dto, product);
        replaceImage(product, image);
        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = requireOwnedProduct(id);
        imageCleanup.afterCommit(product.getImagePublicId());
        productRepository.delete(product);
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

    private Product requireOwnedProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
        currentAccount.checkOwner(product.getUser().getId());
        return product;
    }

    // Giữ ảnh cũ tới khi commit; nếu rollback thì chỉ gỡ ảnh mới vừa upload.
    private void replaceImage(Product product, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        String oldPublicId = product.getImagePublicId();
        var uploaded = imageService.upload(image);
        imageCleanup.afterRollback(uploaded.publicId());
        imageCleanup.afterCommit(oldPublicId);
        product.setImageUrl(uploaded.url());
        product.setImagePublicId(uploaded.publicId());
    }

    private static Pageable pageRequest(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100), Sort.by(Sort.Direction.DESC, "id"));
    }
}
