package vn.iotstar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShopIntegrationTest {

    // Ảnh PNG 1x1 hợp lệ
    private static final String PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jZ1kAAAAASUVORK5CYII=";

    @Autowired MockMvc mvc;
    @Autowired AuthService authService;
    @Autowired UserService userService;
    @Autowired ProductService productService;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OtpTokenRepository otpTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    // ===== Dữ liệu mẫu =====

    private RegisterDTO newRegistration() {
        String id = "test" + System.nanoTime();
        var dto = new RegisterDTO();
        dto.setUsername(id);
        dto.setEmail(id + "@example.com");
        dto.setFullName("Người dùng kiểm thử");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");
        return dto;
    }

    private UserDTO newUser(String prefix) {
        String id = prefix + System.nanoTime();
        var dto = new UserDTO();
        dto.setUsername(id);
        dto.setEmail(id + "@example.com");
        dto.setFullName("Kiểm thử người dùng");
        dto.setRoleName("ROLE_USER");
        dto.setEnabled(true);
        return dto;
    }

    private ProductDTO newProduct() {
        var dto = new ProductDTO();
        dto.setName("Sản phẩm kiểm thử " + System.nanoTime());
        dto.setDescription("Mô tả tiếng Việt");
        dto.setPrice(new BigDecimal("125000.50"));
        return dto;
    }

    private MockMultipartFile image() {
        return new MockMultipartFile("image", "pixel.png", "image/png", Base64.getDecoder().decode(PIXEL_PNG));
    }

    @Test
    @WithUserDetails("admin")
    void productSearchIncludesTheEndOfLongUnicodeDescriptions() {
        var dto = newProduct();
        dto.setDescription("á".repeat(4500) + " Điện THOẠI cuối mô tả");
        var created = productService.create(dto, null);
        try {
            assertThat(productService.findAll("điện thoại cuối", 0, 10).getContent())
                    .extracting(ProductDTO::getId).contains(created.getId());
            dto.setDescription(null);
            productService.update(created.getId(), dto, null);
            assertThat(productService.findAll(dto.getName(), 0, 10).getContent())
                    .extracting(ProductDTO::getId).contains(created.getId());
        } finally {
            productService.delete(created.getId());
        }
    }

    // Đọc OTP từ hộp thư demo data/mailbox/<email>.txt
    private String otpSentTo(String email) throws Exception {
        String mail = Files.readString(Path.of("data", "mailbox", email + ".txt"));
        return mail.split("OTP: ")[1].substring(0, 6);
    }

    private OtpToken tokenOf(String email) {
        return otpTokenRepository.findAll().stream()
                .filter(token -> token.getEmail().equals(email))
                .findFirst()
                .orElseThrow();
    }

    private ResetPasswordDTO resetRequest(String email, String otp, String password, String confirm) {
        var dto = new ResetPasswordDTO();
        dto.setEmail(email);
        dto.setOtp(otp);
        dto.setPassword(password);
        dto.setConfirmPassword(confirm);
        return dto;
    }

    // MockMvc xóa SecurityContext sau request; gán lại để gọi service trực tiếp
    private void restoreTestSecurityContext() {
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
    }

    // ===== Đăng ký, OTP, quên mật khẩu =====

    @Test
    void authPagesAndValidationRender() throws Exception {
        for (String url : new String[] {"/register", "/verify-otp", "/forgot-password", "/reset-password"}) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
        mvc.perform(post("/register").with(csrf()).param("email", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void registrationRequiresOtpAndOtpIsSingleUse() throws Exception {
        var dto = newRegistration();
        authService.register(dto);
        assertThat(userRepository.findByEmailIgnoreCase(dto.getEmail()).orElseThrow().isEnabled()).isFalse();

        assertThat(authService.verifyRegister(dto.getEmail(), otpSentTo(dto.getEmail()))).isTrue();
        assertThat(userRepository.findByEmailIgnoreCase(dto.getEmail()).orElseThrow().isEnabled()).isTrue();
        assertThat(authService.verifyRegister(dto.getEmail(), otpSentTo(dto.getEmail()))).isFalse();
    }

    @Test
    void duplicateAndMismatchRegistrationRejected() {
        var dto = newRegistration();
        dto.setConfirmPassword("abcdef");
        assertThatThrownBy(() -> authService.register(dto)).isInstanceOf(IllegalArgumentException.class);

        dto.setConfirmPassword("123456");
        authService.register(dto);
        assertThatThrownBy(() -> authService.register(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void wrongOtpCountsAttemptsAndLocksAfterFive() throws Exception {
        var dto = newRegistration();
        authService.register(dto);
        String valid = otpSentTo(dto.getEmail());
        String wrong = valid.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            assertThat(authService.verifyRegister(dto.getEmail(), wrong)).isFalse();
        }
        assertThat(tokenOf(dto.getEmail()).getAttempts()).isEqualTo(5);
        assertThat(authService.verifyRegister(dto.getEmail(), valid)).isFalse();
    }

    @Test
    void expiredOtpRejected() throws Exception {
        var dto = newRegistration();
        authService.register(dto);
        var token = tokenOf(dto.getEmail());
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        otpTokenRepository.save(token);

        assertThat(authService.verifyRegister(dto.getEmail(), otpSentTo(dto.getEmail()))).isFalse();
    }

    @Test
    void resendInvalidatesPreviousOtpAndHasCooldown() throws Exception {
        var dto = newRegistration();
        authService.register(dto);
        assertThatThrownBy(() -> authService.resendRegisterOtp(dto.getEmail()))
                .isInstanceOf(IllegalArgumentException.class);

        // Giả lập đã qua 60 giây
        var token = tokenOf(dto.getEmail());
        Long oldId = token.getId();
        token.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        otpTokenRepository.save(token);

        authService.resendRegisterOtp(dto.getEmail());
        assertThat(otpTokenRepository.existsById(oldId)).isFalse();
        assertThat(authService.verifyRegister(dto.getEmail(), otpSentTo(dto.getEmail()))).isTrue();
    }

    @Test
    void resetPasswordNeedsMatchingPasswordsAndIsSingleUse() throws Exception {
        var dto = newRegistration();
        authService.register(dto);
        assertThat(authService.verifyRegister(dto.getEmail(), otpSentTo(dto.getEmail()))).isTrue();
        authService.forgotPassword(dto.getEmail());
        String otp = otpSentTo(dto.getEmail());

        var mismatch = resetRequest(dto.getEmail(), otp, "newpassword", "different");
        assertThatThrownBy(() -> authService.resetPassword(mismatch)).isInstanceOf(IllegalArgumentException.class);

        var request = resetRequest(dto.getEmail(), otp, "newpassword", "newpassword");
        assertThat(authService.resetPassword(request)).isTrue();
        assertThat(authService.resetPassword(request)).isFalse();
        String hash = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElseThrow().getPassword();
        assertThat(passwordEncoder.matches("newpassword", hash)).isTrue();
    }

    @Test
    void registerOtpCannotResetPassword() throws Exception {
        var dto = newRegistration();
        authService.register(dto);
        var request = resetRequest(dto.getEmail(), otpSentTo(dto.getEmail()), "newpassword", "newpassword");
        assertThat(authService.resetPassword(request)).isFalse();
    }

    // ===== Quản lý user =====

    @Test
    @WithUserDetails("admin")
    void adminViewsRender() throws Exception {
        for (String url : new String[] {"/users", "/users/create", "/products", "/products/create",
                "/users?keyword=noresult", "/products?keyword=noresult"}) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
        mvc.perform(get("/users").param("size", "1"))
                .andExpect(content().string(containsString("pagination")));
    }

    @Test
    @WithUserDetails("admin")
    void userCrudSearchPaginationAndProductCount() {
        var created = userService.create(newUser("crud"));
        assertThat(created.getId()).isNotNull();
        assertThat(userService.findAll(created.getUsername(), 0, 1).getTotalElements()).isEqualTo(1);

        created.setFullName("Họ tên đã sửa");
        userService.update(created.getId(), created);
        assertThat(userService.findById(created.getId()).getFullName()).isEqualTo("Họ tên đã sửa");
        assertThat(userService.countProducts(created.getId())).isZero();

        userService.delete(created.getId());
        assertThat(userRepository.existsById(created.getId())).isFalse();
    }

    @Test
    @WithUserDetails("admin")
    void editingUserFromFormKeepsAvatar() {
        var created = userService.create(newUser("avatar"));

        // Form sửa user không gửi trường images
        var form = newUser("avatar");
        form.setUsername(created.getUsername());
        form.setEmail(created.getEmail());
        form.setFullName("Đổi họ tên");
        userService.update(created.getId(), form);

        String images = userRepository.findById(created.getId()).orElseThrow().getImages();
        assertThat(images).isEqualTo("/images/avatar.svg");
    }

    @Test
    @WithUserDetails("admin")
    void duplicateUsernameAndSelfDeleteRejected() {
        var admin = userRepository.findByUsernameIgnoreCase("admin").orElseThrow();
        var dto = userService.findById(admin.getId());
        dto.setUsername("user01");
        assertThatThrownBy(() -> userService.update(admin.getId(), dto)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> userService.delete(admin.getId())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @WithUserDetails("admin")
    void deletingUserCascadesProducts() {
        var created = userService.create(newUser("cascade"));
        var product = new Product();
        product.setName("Sản phẩm liên kết");
        product.setPrice(BigDecimal.ONE);
        product.setUser(userRepository.findById(created.getId()).orElseThrow());
        productRepository.save(product);
        assertThat(userService.countProducts(created.getId())).isEqualTo(1);

        userService.delete(created.getId());
        assertThat(productRepository.existsById(product.getId())).isFalse();
    }

    // ===== Quản lý sản phẩm =====

    @Test
    @WithUserDetails("user01")
    void productCrudUploadReplaceSearchAndCount() throws Exception {
        var dto = newProduct();
        var created = productService.create(dto, image());
        assertThat(created.getUsername()).isEqualTo("user01");
        assertThat(created.getImageUrl()).startsWith("/uploads/");
        String oldImage = productRepository.findById(created.getId()).orElseThrow().getImagePublicId();
        assertThat(Files.exists(Path.of("uploads", oldImage))).isTrue();

        assertThat(productService.findAll(dto.getName(), 0, 1).getTotalElements()).isEqualTo(1);
        assertThat(userService.countProducts(created.getUserId())).isPositive();

        // Sửa kèm ảnh mới: ảnh cũ bị xóa
        dto.setName("Tên sản phẩm đã sửa");
        var edited = productService.update(created.getId(), dto, image());
        assertThat(edited.getName()).isEqualTo("Tên sản phẩm đã sửa");
        assertThat(Files.exists(Path.of("uploads", oldImage))).isFalse();

        mvc.perform(get("/products/edit/" + created.getId())).andExpect(status().isOk());
        restoreTestSecurityContext();

        String currentImage = productRepository.findById(created.getId()).orElseThrow().getImagePublicId();
        productService.delete(created.getId());
        assertThat(productRepository.existsById(created.getId())).isFalse();
        assertThat(Files.exists(Path.of("uploads", currentImage))).isFalse();
    }

    @Test
    @WithUserDetails("user01")
    void cannotAccessOtherUsersProduct() {
        var product = new Product();
        product.setName("Sản phẩm admin");
        product.setPrice(BigDecimal.ONE);
        product.setUser(userRepository.findByUsernameIgnoreCase("admin").orElseThrow());
        productRepository.save(product);

        assertThatThrownBy(() -> productService.findById(product.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> productService.delete(product.getId())).isInstanceOf(AccessDeniedException.class);
        assertThat(productService.findAll("Sản phẩm admin", 0, 10).getTotalElements()).isZero();
        productRepository.deleteById(product.getId());
    }

    @Test
    @WithUserDetails("admin")
    void invalidProductFormAndFakeImageRejected() throws Exception {
        mvc.perform(multipart("/products/create").with(csrf()).param("name", "").param("price", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/form"));
        restoreTestSecurityContext();

        var fake = new MockMultipartFile("image", "bad.png", "image/png", "not an image".getBytes());
        assertThatThrownBy(() -> productService.create(newProduct(), fake)).isInstanceOf(IllegalArgumentException.class);
    }
}
