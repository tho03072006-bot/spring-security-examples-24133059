package vn.iotstar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.ProductService;

// Giả lập dịch vụ ngoài bị lỗi, giữ nguyên transaction và repository thật để kiểm tra rollback.
@SpringBootTest
@ActiveProfiles("test")
class ExternalServiceFailureIntegrationTest {

    @Autowired AuthService authService;
    @Autowired ProductService productService;
    @Autowired UserRepository users;
    @Autowired ProductRepository products;
    @Autowired OtpTokenRepository tokens;
    @MockitoBean EmailService emailService;
    @MockitoBean CloudinaryService imageService;

    @Test
    void failedEmailDoesNotLeaveAnUnusableRegistration() {
        var dto = new RegisterDTO();
        dto.setUsername("mailfail" + System.nanoTime());
        dto.setEmail(dto.getUsername() + "@example.com");
        dto.setFullName("Lỗi gửi mail");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");
        doThrow(new MailSendException("SMTP unavailable")).when(emailService).sendOtp(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> authService.register(dto)).isInstanceOf(MailSendException.class);
        assertThat(users.existsByUsernameIgnoreCase(dto.getUsername())).isFalse();
        assertThat(tokens.findAll().stream().filter(token -> token.getEmail().equals(dto.getEmail()))).isEmpty();
    }

    @Test
    @WithUserDetails("admin")
    void failedImageUploadPreservesProductAndItsPreviousImage() {
        var dto = new ProductDTO();
        dto.setName("Ảnh ban đầu");
        dto.setPrice(BigDecimal.TEN);
        var file = new MockMultipartFile("image", "example.png", "image/png", new byte[] {1});
        when(imageService.upload(any())).thenReturn(new CloudinaryUploadResult("https://example.com/old.png", "old-image"));
        var saved = productService.create(dto, file);
        try {
            dto.setName("Thay đổi không được lưu");
            when(imageService.upload(any())).thenThrow(new IllegalStateException("Cloudinary unavailable"));
            assertThatThrownBy(() -> productService.update(saved.getId(), dto, file)).isInstanceOf(IllegalStateException.class);
            var unchanged = products.findById(saved.getId()).orElseThrow();
            assertThat(unchanged.getName()).isEqualTo("Ảnh ban đầu");
            assertThat(unchanged.getImagePublicId()).isEqualTo("old-image");
            verify(imageService, never()).delete("old-image");
        } finally {
            productService.delete(saved.getId());
        }
    }
}
