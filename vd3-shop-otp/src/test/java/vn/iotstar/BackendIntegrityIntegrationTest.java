package vn.iotstar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

// Kiểm thử request/session qua bộ lọc Spring Security; profile test dùng DB H2 và ảnh/mail local.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendIntegrityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired AuthService authService;
    @Autowired UserService userService;
    @Autowired ProductService productService;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ProductRepository products;
    @Autowired OtpTokenRepository tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDetailsService userDetailsService;
    @Autowired SessionRegistry sessionRegistry;
    @Autowired PlatformTransactionManager transactionManager;

    private final List<Long> createdUsers = new ArrayList<>();
    private final List<Long> createdProducts = new ArrayList<>();

    @AfterEach
    void cleanFixtures() {
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            createdProducts.forEach(id -> {
                if (products.existsById(id)) products.deleteById(id);
            });
            createdUsers.forEach(id -> users.findById(id).ifPresent(user -> {
                tokens.deleteByEmail(user.getEmail());
                users.delete(user);
            }));
        });
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof vn.iotstar.security.CustomUserDetails user
                        && createdUsers.contains(user.getId()))
                .forEach(principal -> sessionRegistry.getAllSessions(principal, true)
                        .forEach(session -> sessionRegistry.removeSessionInformation(session.getSessionId())));
        SecurityContextHolder.clearContext();
    }

    private User account(String role) {
        var user = new User();
        user.setUsername("audit" + System.nanoTime());
        user.setEmail(user.getUsername() + "@example.com");
        user.setFullName("Tài khoản kiểm thử");
        user.setPassword(encoder.encode("123456"));
        user.setEnabled(true);
        user.setRole(roles.findByName(role).orElseThrow());
        user = users.save(user);
        createdUsers.add(user.getId());
        return user;
    }

    private Product product(User owner, String name) {
        var product = new Product();
        product.setUser(owner);
        product.setName(name);
        product.setPrice(new BigDecimal("100000.50"));
        product = products.save(product);
        createdProducts.add(product.getId());
        return product;
    }

    private ProductDTO productForm() {
        var dto = new ProductDTO();
        dto.setName("Sản phẩm " + System.nanoTime());
        dto.setPrice(new BigDecimal("199000.25"));
        return dto;
    }

    private MockMultipartFile image() {
        return new MockMultipartFile("image", "pixel.png", "image/png", Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jZ1kAAAAASUVORK5CYII="));
    }

    private Path imagePath(String publicId) {
        return Path.of("uploads", publicId);
    }

    private String imageId(Long productId) {
        return products.findById(productId).orElseThrow().getImagePublicId();
    }

    private void asAdmin() {
        var principal = userDetailsService.loadUserByUsername("admin");
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    private MockHttpSession login(String username, String password) throws Exception {
        var result = mvc.perform(formLogin().user(username).password(password))
                .andExpect(authenticated()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private void expectExpired(MockHttpSession session) throws Exception {
        mvc.perform(get("/products").session(session))
                .andExpect(redirectedUrl("/login?expired")).andExpect(unauthenticated());
    }

    private String otp(String email) throws Exception {
        String mail = Files.readString(Path.of("data", "mailbox", email + ".txt"));
        return mail.split("OTP: ")[1].substring(0, 6);
    }

    private ResetPasswordDTO reset(String email, String otp, String password) {
        var dto = new ResetPasswordDTO();
        dto.setEmail(email);
        dto.setOtp(otp);
        dto.setPassword(password);
        dto.setConfirmPassword(password);
        return dto;
    }

    @Test
    void disablingAccountRevokesExistingSessionAndRejectsNewLogin() throws Exception {
        var user = account("ROLE_USER");
        var session = login(user.getUsername(), "123456");
        asAdmin();
        var dto = userService.findById(user.getId());
        dto.setEnabled(false);
        userService.update(user.getId(), dto);
        expectExpired(session);
        mvc.perform(formLogin().user(user.getUsername()).password("123456")).andExpect(unauthenticated());
    }

    @Test
    void demotedAdminCannotUsePermissionsFromOldSession() throws Exception {
        var user = account("ROLE_ADMIN");
        var session = login(user.getUsername(), "123456");
        asAdmin();
        var dto = userService.findById(user.getId());
        dto.setRoleName("ROLE_USER");
        userService.update(user.getId(), dto);
        expectExpired(session);
        mvc.perform(get("/users").session(login(user.getUsername(), "123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletingAccountRevokesItsSession() throws Exception {
        var user = account("ROLE_USER");
        var session = login(user.getUsername(), "123456");
        asAdmin();
        userService.delete(user.getId());
        expectExpired(session);
        assertThat(users.existsById(user.getId())).isFalse();
    }

    @Test
    void resetPasswordRevokesSessionAndReplacesOldPassword() throws Exception {
        var user = account("ROLE_USER");
        var session = login(user.getUsername(), "123456");
        authService.forgotPassword(user.getEmail());
        assertThat(authService.resetPassword(reset(user.getEmail(), otp(user.getEmail()), "new-password"))).isTrue();
        expectExpired(session);
        mvc.perform(formLogin().user(user.getUsername()).password("123456")).andExpect(unauthenticated());
        login(user.getUsername(), "new-password");
    }

    @Test
    void changingEmailInvalidatesOldOtpBeforeEmailCanBeReused() throws Exception {
        var user = account("ROLE_USER");
        authService.forgotPassword(user.getEmail());
        String oldOtp = otp(user.getEmail());
        var session = login(user.getUsername(), "123456");
        asAdmin();
        var dto = userService.findById(user.getId());
        dto.setEmail("new-" + user.getEmail());
        userService.update(user.getId(), dto);
        expectExpired(session);

        var secondUser = account("ROLE_USER");
        secondUser.setEmail(user.getEmail());
        users.save(secondUser);
        assertThat(authService.resetPassword(reset(user.getEmail(), oldOtp, "stolen-password"))).isFalse();
        assertThat(encoder.matches("123456", users.findById(secondUser.getId()).orElseThrow().getPassword())).isTrue();
    }

    @Test
    void rolledBackAccountUpdateKeepsSessionValid() throws Exception {
        var user = account("ROLE_USER");
        var session = login(user.getUsername(), "123456");
        asAdmin();
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            var dto = userService.findById(user.getId());
            dto.setEnabled(false);
            userService.update(user.getId(), dto);
            transaction.setRollbackOnly();
        });
        assertThat(users.findById(user.getId()).orElseThrow().isEnabled()).isTrue();
        mvc.perform(get("/products").session(session)).andExpect(status().isOk()).andExpect(authenticated());
    }

    @Test
    @WithUserDetails("admin")
    void replacingImageThenRollingBackPreservesOldImageAndRemovesNewUpload() {
        var dto = productForm();
        var saved = productService.create(dto, image());
        String oldImage = imageId(saved.getId());
        var newImage = new AtomicReference<String>();
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
                dto.setName("Tên không được commit");
                productService.update(saved.getId(), dto, image());
                newImage.set(imageId(saved.getId()));
                assertThat(imagePath(oldImage)).exists();
                transaction.setRollbackOnly();
            });
            assertThat(imageId(saved.getId())).isEqualTo(oldImage);
            assertThat(productService.findById(saved.getId()).getName()).isEqualTo(saved.getName());
            assertThat(imagePath(oldImage)).exists();
            assertThat(imagePath(newImage.get())).doesNotExist();
        } finally {
            productService.delete(saved.getId());
        }
    }

    @Test
    @WithUserDetails("admin")
    void rollbackAfterCreatingProductRemovesOrphanUpload() {
        var id = new AtomicReference<Long>();
        var uploaded = new AtomicReference<String>();
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            var saved = productService.create(productForm(), image());
            id.set(saved.getId());
            uploaded.set(imageId(saved.getId()));
            transaction.setRollbackOnly();
        });
        assertThat(products.existsById(id.get())).isFalse();
        assertThat(imagePath(uploaded.get())).doesNotExist();
    }

    @Test
    @WithUserDetails("admin")
    void productDeletionOnlyRemovesImageAfterSuccessfulCommit() {
        var saved = productService.create(productForm(), image());
        String oldImage = imageId(saved.getId());
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
                productService.delete(saved.getId());
                assertThat(imagePath(oldImage)).exists();
                transaction.setRollbackOnly();
            });
            assertThat(products.existsById(saved.getId())).isTrue();
            assertThat(imagePath(oldImage)).exists();
        } finally {
            productService.delete(saved.getId());
        }
        assertThat(imagePath(oldImage)).doesNotExist();
    }

    @Test
    @WithUserDetails("admin")
    void deletingUserCascadesImagesOnlyAfterCommit() {
        var user = account("ROLE_USER");
        var saved = productService.create(productForm(), image());
        var product = products.findById(saved.getId()).orElseThrow();
        product.setUser(user);
        products.save(product);
        String oldImage = product.getImagePublicId();
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            userService.delete(user.getId());
            assertThat(imagePath(oldImage)).exists();
            transaction.setRollbackOnly();
        });
        assertThat(users.existsById(user.getId())).isTrue();
        assertThat(products.existsById(saved.getId())).isTrue();
        assertThat(imagePath(oldImage)).exists();
        userService.delete(user.getId());
        assertThat(products.existsById(saved.getId())).isFalse();
        assertThat(imagePath(oldImage)).doesNotExist();
    }

    private List<Boolean> race(Callable<Boolean> action) throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> concurrentAction = () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Start timeout");
                return action.call();
            };
            var first = executor.submit(concurrentAction);
            var second = executor.submit(concurrentAction);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
    }

    @Test
    void simultaneousFirstOtpRequestsRespectCooldown() throws Exception {
        var user = account("ROLE_USER");
        var results = race(() -> {
            try {
                authService.forgotPassword(user.getEmail());
                return true;
            } catch (IllegalArgumentException exception) {
                assertThat(exception).hasMessageContaining("60 giây");
                return false;
            }
        });
        assertThat(results).containsExactlyInAnyOrder(true, false);
        assertThat(tokens.findAll().stream().filter(token -> token.getEmail().equals(user.getEmail()))).hasSize(1);
        assertThat(authService.resetPassword(reset(user.getEmail(), otp(user.getEmail()), "new-password"))).isTrue();
    }

    @Test
    void simultaneousOtpVerificationSucceedsExactlyOnce() throws Exception {
        var dto = new RegisterDTO();
        dto.setUsername("race" + System.nanoTime());
        dto.setEmail(dto.getUsername() + "@example.com");
        dto.setFullName("Kiểm thử OTP đồng thời");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");
        authService.register(dto);
        createdUsers.add(users.findByUsernameIgnoreCase(dto.getUsername()).orElseThrow().getId());
        String code = otp(dto.getEmail());
        assertThat(race(() -> authService.verifyRegister(dto.getEmail(), code)))
                .containsExactlyInAnyOrder(true, false);
        assertThat(users.findByEmailIgnoreCase(dto.getEmail()).orElseThrow().isEnabled()).isTrue();
    }

    @Test
    void unicodePasswordLimitDoesNotConsumeValidOtp() throws Exception {
        var user = account("ROLE_USER");
        authService.forgotPassword(user.getEmail());
        String code = otp(user.getEmail());
        assertThatThrownBy(() -> authService.resetPassword(reset(user.getEmail(), code, "ế".repeat(30))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Hãy rút ngắn mật khẩu");
        assertThat(tokens.findAll().stream().filter(token -> token.getEmail().equals(user.getEmail())))
                .allSatisfy(token -> {
                    assertThat(token.isUsed()).isFalse();
                    assertThat(token.getAttempts()).isZero();
                });
        assertThat(authService.resetPassword(reset(user.getEmail(), code, "Mật-khẩu-123"))).isTrue();
        login(user.getUsername(), "Mật-khẩu-123");
    }

    @Test
    void invalidRegistrationDoesNotPersistUser() throws Exception {
        String username = "invalid" + System.nanoTime();
        String password = "ế".repeat(30);
        mvc.perform(post("/register").with(csrf()).param("username", username)
                        .param("email", username + "@example.com").param("fullName", "Kiểm thử")
                        .param("password", password).param("confirmPassword", password))
                .andExpect(status().isOk()).andExpect(view().name("auth/register"));
        assertThat(users.existsByUsernameIgnoreCase(username)).isFalse();
    }

    @Test
    @WithUserDetails("user01")
    void submittedProductOwnerIdAndImageUrlCannotOverrideServerValues() throws Exception {
        var admin = users.findByUsernameIgnoreCase("admin").orElseThrow();
        var original = product(admin, "Sản phẩm của admin");
        String name = "tamper" + System.nanoTime();
        mvc.perform(multipart("/products/create").with(csrf()).param("name", name).param("price", "9.99")
                        .param("id", original.getId().toString()).param("userId", admin.getId().toString())
                        .param("imageUrl", "https://example.com/forged.png"))
                .andExpect(redirectedUrl("/products"));
        var saved = products.findAll().stream().filter(product -> name.equals(product.getName())).findFirst().orElseThrow();
        createdProducts.add(saved.getId());
        assertThat(saved.getId()).isNotEqualTo(original.getId());
        assertThat(saved.getUser().getId()).isEqualTo(users.findByUsernameIgnoreCase("user01").orElseThrow().getId());
        assertThat(saved.getImageUrl()).isNull();
        assertThat(products.findById(original.getId()).orElseThrow().getName()).isEqualTo(original.getName());
    }

    @Test
    @WithUserDetails("user01")
    void userCannotReadEditOrDeleteAnotherOwnersProductViaHttp() throws Exception {
        var original = product(users.findByUsernameIgnoreCase("admin").orElseThrow(), "Chủ sở hữu khác");
        mvc.perform(get("/products/edit/" + original.getId())).andExpect(status().isForbidden());
        mvc.perform(multipart("/products/edit/" + original.getId()).with(csrf())
                        .param("name", "Tên giả mạo").param("price", "1"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/products/delete/" + original.getId()).with(csrf())).andExpect(status().isForbidden());
        assertThat(products.findById(original.getId()).orElseThrow().getName()).isEqualTo(original.getName());
    }

    @Test
    @WithUserDetails("user01")
    void userCannotMutateAccountsThroughHttpOrService() throws Exception {
        Long adminId = users.findByUsernameIgnoreCase("admin").orElseThrow().getId();
        for (String route : List.of("/users/create", "/users/edit/" + adminId, "/users/delete/" + adminId)) {
            mvc.perform(post(route).with(csrf()).param("username", "fakeadmin").param("roleName", "ROLE_ADMIN"))
                    .andExpect(status().isForbidden());
        }
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext());
        assertThatThrownBy(() -> userService.create(new UserDTO())).isInstanceOf(AccessDeniedException.class);
        assertThat(users.existsById(adminId)).isTrue();
    }

    @Test
    @WithUserDetails("user01")
    void productPaginationSearchAndCountsStayScopedToOwner() {
        String keyword = "pagination" + System.nanoTime();
        var owner = users.findByUsernameIgnoreCase("user01").orElseThrow();
        var other = users.findByUsernameIgnoreCase("admin").orElseThrow();
        long before = productService.countByUser(owner.getId());
        var ids = new ArrayList<Long>();
        for (int index = 0; index < 13; index++) ids.add(product(owner, keyword + " " + index).getId());
        for (int index = 0; index < 4; index++) product(other, keyword + " foreign " + index);
        var first = productService.findAll(keyword.toUpperCase(), 0, 5);
        var second = productService.findAll(keyword, 1, 5);
        var last = productService.findAll(keyword, 2, 5);
        assertThat(first.getTotalElements()).isEqualTo(13);
        assertThat(first.getTotalPages()).isEqualTo(3);
        assertThat(first.getContent()).extracting(ProductDTO::getId)
                .containsExactly(ids.get(12), ids.get(11), ids.get(10), ids.get(9), ids.get(8));
        assertThat(second.getContent()).extracting(ProductDTO::getId)
                .containsExactly(ids.get(7), ids.get(6), ids.get(5), ids.get(4), ids.get(3));
        assertThat(last.getContent()).extracting(ProductDTO::getId).containsExactly(ids.get(2), ids.get(1), ids.get(0));
        assertThat(productService.countByUser(owner.getId())).isEqualTo(before + 13);
        assertThat(productService.findAll("' OR 1=1 --", 0, 5).getTotalElements()).isZero();
    }

    @Test
    @WithUserDetails("admin")
    void userPaginationSearchAndProductCountsAreAccurate() {
        String keyword = "Nhóm kiểm thử " + System.nanoTime();
        long before = userService.countUsers();
        var group = new ArrayList<User>();
        for (int index = 0; index < 5; index++) {
            var user = account("ROLE_USER");
            user.setFullName(keyword + " " + index);
            group.add(users.save(user));
        }
        product(group.getLast(), "Sản phẩm thứ nhất");
        product(group.getLast(), "Sản phẩm thứ hai");
        var first = userService.findAll(keyword.toUpperCase(), 0, 2);
        var last = userService.findAll(keyword, 2, 2);
        assertThat(first.getTotalElements()).isEqualTo(5);
        assertThat(first.getTotalPages()).isEqualTo(3);
        assertThat(first.getContent()).extracting(UserDTO::getId)
                .containsExactly(group.get(4).getId(), group.get(3).getId());
        assertThat(first.getContent().getFirst().getProductCount()).isEqualTo(2);
        assertThat(last.getContent()).extracting(UserDTO::getId).containsExactly(group.getFirst().getId());
        assertThat(userService.countUsers()).isEqualTo(before + 5);
    }

    @Test
    void secondLoginExpiresFirstSession() throws Exception {
        var user = account("ROLE_USER");
        var first = login(user.getUsername(), "123456");
        var second = login(user.getUsername(), "123456");
        expectExpired(first);
        mvc.perform(get("/products").session(second)).andExpect(status().isOk()).andExpect(authenticated());
    }

    @Test
    void loginRotatesAnonymousSessionId() throws Exception {
        var user = account("ROLE_USER");
        var anonymous = new MockHttpSession();
        String before = anonymous.getId();
        var result = mvc.perform(post("/login").session(anonymous).with(csrf())
                        .param("username", user.getUsername()).param("password", "123456"))
                .andExpect(authenticated()).andReturn();
        assertThat(result.getRequest().getSession(false).getId()).isNotEqualTo(before);
    }
}
