package vn.iotstar;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import vn.iotstar.dto.*;
import vn.iotstar.repository.*;
import vn.iotstar.service.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.containsString;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ShopIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    @Autowired OtpService otp;
    @Autowired UserService userService;
    @Autowired ProductService productService;
    @Autowired UserRepository users;
    @Autowired ProductRepository products;
    @Autowired OtpTokenRepository tokens;
    @Autowired PasswordEncoder encoder;
    private RegisterDTO registerDTO(){String id="test"+System.nanoTime();var d=new RegisterDTO();d.setUsername(id);d.setEmail(id+"@example.com");d.setFullName("Người dùng kiểm thử");d.setPassword("123456");d.setConfirmPassword("123456");return d;}
    private String code(String email) throws Exception {return Files.readString(Path.of("data/mailbox",email+".txt")).split("OTP: ")[1].substring(0,6);}
    private ProductDTO productDTO(){var d=new ProductDTO();d.setName("Sản phẩm kiểm thử "+System.nanoTime());d.setDescription("Mô tả tiếng Việt");d.setPrice(new BigDecimal("125000.50"));return d;}
    private MockMultipartFile image(){return new MockMultipartFile("image","pixel.png","image/png",Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jZ1kAAAAASUVORK5CYII="));}
    @Test void authPagesAndValidationRender() throws Exception {
        for(String url:new String[]{"/register","/verify-otp","/forgot-password","/reset-password"})mvc.perform(get(url)).andExpect(status().isOk());
        mvc.perform(post("/register").with(csrf()).param("email","invalid")).andExpect(status().isOk()).andExpect(view().name("auth/register"));
    }
    @Test void registrationRequiresOtpAndOtpIsSingleUse() throws Exception {
        var d=registerDTO();auth.register(d);assertThat(users.findByEmailIgnoreCase(d.getEmail()).orElseThrow().isEnabled()).isFalse();
        assertThat(auth.verifyRegister(d.getEmail(),code(d.getEmail()))).isTrue();
        assertThat(users.findByEmailIgnoreCase(d.getEmail()).orElseThrow().isEnabled()).isTrue();
        assertThat(auth.verifyRegister(d.getEmail(),code(d.getEmail()))).isFalse();
    }
    @Test void duplicateAndMismatchRegistrationRejected(){
        var d=registerDTO();d.setConfirmPassword("abcdef");assertThatThrownBy(()->auth.register(d)).isInstanceOf(IllegalArgumentException.class);
        d.setConfirmPassword("123456");auth.register(d);assertThatThrownBy(()->auth.register(d)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void wrongOtpPersistsAttemptsAndLocksAfterFive() throws Exception {
        var d=registerDTO();auth.register(d);String valid=code(d.getEmail());String wrong=valid.equals("000000")?"111111":"000000";
        for(int i=0;i<5;i++)assertThat(auth.verifyRegister(d.getEmail(),wrong)).isFalse();
        assertThat(tokens.findAll().stream().filter(t->t.getEmail().equals(d.getEmail())).findFirst().orElseThrow().getAttempts()).isEqualTo(5);
        assertThat(auth.verifyRegister(d.getEmail(),valid)).isFalse();
    }
    @Test void expiredOtpRejected() throws Exception {
        var d=registerDTO();auth.register(d);var t=tokens.findAll().stream().filter(x->x.getEmail().equals(d.getEmail())).findFirst().orElseThrow();t.setExpiresAt(LocalDateTime.now().minusSeconds(1));tokens.save(t);
        assertThat(auth.verifyRegister(d.getEmail(),code(d.getEmail()))).isFalse();
    }
    @Test void resendInvalidatesPreviousOtpAndHasCooldown() throws Exception {
        var d=registerDTO();auth.register(d);String old=code(d.getEmail());
        assertThatThrownBy(()->auth.resendRegisterOtp(d.getEmail())).isInstanceOf(IllegalArgumentException.class);
        var t=tokens.findAll().stream().filter(x->x.getEmail().equals(d.getEmail())).findFirst().orElseThrow();Long oldId=t.getId();t.setCreatedAt(LocalDateTime.now().minusMinutes(2));tokens.save(t);
        auth.resendRegisterOtp(d.getEmail());assertThat(tokens.existsById(oldId)).isFalse();assertThat(auth.verifyRegister(d.getEmail(),code(d.getEmail()))).isTrue();
    }
    @Test void resetPasswordNeedsMatchingPurposeAndIsSingleUse() throws Exception {
        var d=registerDTO();auth.register(d);String registerCode=code(d.getEmail());assertThat(auth.verifyRegister(d.getEmail(),registerCode)).isTrue();auth.forgotPassword(d.getEmail());
        var r=new ResetPasswordDTO();r.setEmail(d.getEmail());r.setPassword("newpassword");r.setConfirmPassword("different");r.setOtp(code(d.getEmail()));
        assertThatThrownBy(()->auth.resetPassword(r)).isInstanceOf(IllegalArgumentException.class);
        r.setConfirmPassword("newpassword");assertThat(auth.resetPassword(r)).isTrue();assertThat(auth.resetPassword(r)).isFalse();
        assertThat(encoder.matches("newpassword",users.findByEmailIgnoreCase(d.getEmail()).orElseThrow().getPassword())).isTrue();
    }
    @Test void registerCodeCannotResetPassword() throws Exception {
        var d=registerDTO();auth.register(d);var r=new ResetPasswordDTO();r.setEmail(d.getEmail());r.setPassword("newpassword");r.setConfirmPassword("newpassword");r.setOtp(code(d.getEmail()));assertThat(auth.resetPassword(r)).isFalse();
    }
    @Test @WithUserDetails("admin") void adminViewsRender() throws Exception {
        for(String url:new String[]{"/users","/users/create","/products","/products/create","/users?keyword=noresult","/products?keyword=noresult"})mvc.perform(get(url)).andExpect(status().isOk());
        mvc.perform(get("/users").param("size","1")).andExpect(content().string(containsString("pagination")));
    }
    @Test @WithUserDetails("admin") void userCrudSearchPaginationAndProductCounts() {
        var d=new UserDTO();String id="crud"+System.nanoTime();d.setUsername(id);d.setEmail(id+"@example.com");d.setFullName("Kiểm thử người dùng");d.setRoleName("ROLE_USER");d.setEnabled(true);
        var saved=userService.create(d);assertThat(saved.getId()).isNotNull();assertThat(userService.findAll(id,0,1).getTotalElements()).isEqualTo(1);
        saved.setFullName("Họ tên đã sửa");userService.update(saved.getId(),saved);assertThat(userService.findById(saved.getId()).getFullName()).isEqualTo("Họ tên đã sửa");
        assertThat(userService.countProducts(saved.getId())).isZero();userService.delete(saved.getId());assertThat(users.existsById(saved.getId())).isFalse();
    }
    @Test @WithUserDetails("admin") void userEditDuplicateAndSelfDeleteRejected() {
        var admin=users.findByUsernameIgnoreCase("admin").orElseThrow();var dto=userService.findById(admin.getId());dto.setUsername("user01");assertThatThrownBy(()->userService.update(admin.getId(),dto)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->userService.delete(admin.getId())).isInstanceOf(IllegalArgumentException.class);
    }
    @Test @WithUserDetails("user01") void productCrudUploadReplaceSearchAndCounts() throws Exception {
        var d=productDTO();var p=productService.create(d,image());assertThat(p.getUsername()).isEqualTo("user01");assertThat(p.getImageUrl()).startsWith("/uploads/");
        String old=products.findById(p.getId()).orElseThrow().getImagePublicId();assertThat(Files.exists(Path.of("uploads",old))).isTrue();
        assertThat(productService.findAll(d.getName(),0,1).getTotalElements()).isEqualTo(1);assertThat(userService.countProducts(p.getUserId())).isGreaterThan(0);
        d.setName("Tên sản phẩm đã sửa");var edited=productService.update(p.getId(),d,image());assertThat(edited.getName()).isEqualTo("Tên sản phẩm đã sửa");assertThat(Files.exists(Path.of("uploads",old))).isFalse();
        mvc.perform(get("/products/edit/"+p.getId())).andExpect(status().isOk());String current=products.findById(p.getId()).orElseThrow().getImagePublicId();
        org.springframework.security.core.context.SecurityContextHolder.setContext(org.springframework.security.test.context.TestSecurityContextHolder.getContext());
        productService.delete(p.getId());assertThat(products.existsById(p.getId())).isFalse();assertThat(Files.exists(Path.of("uploads",current))).isFalse();
    }
    @Test @WithUserDetails("user01") void cannotEditOtherUsersProduct() {
        var admin=users.findByUsernameIgnoreCase("admin").orElseThrow();var p=new vn.iotstar.entity.Product();p.setName("Sản phẩm admin");p.setPrice(BigDecimal.ONE);p.setUser(admin);products.save(p);
        assertThatThrownBy(()->productService.findById(p.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(()->productService.delete(p.getId())).isInstanceOf(AccessDeniedException.class);
        assertThat(productService.findAll("Sản phẩm admin",0,10).getTotalElements()).isZero();products.deleteById(p.getId());
    }
    @Test @WithUserDetails("admin") void invalidProductFormAndImageRejected() throws Exception {
        mvc.perform(multipart("/products/create").with(csrf()).param("name","").param("price","-1")).andExpect(status().isOk()).andExpect(view().name("products/form"));
        org.springframework.security.core.context.SecurityContextHolder.setContext(org.springframework.security.test.context.TestSecurityContextHolder.getContext());
        var fake=new MockMultipartFile("image","bad.png","image/png","not an image".getBytes());assertThatThrownBy(()->productService.create(productDTO(),fake)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test @WithUserDetails("admin") void deletingUserCascadesProducts() {
        var d=new UserDTO();String id="cascade"+System.nanoTime();d.setUsername(id);d.setEmail(id+"@example.com");d.setFullName("Xóa liên kết");d.setEnabled(true);d.setRoleName("ROLE_USER");var saved=userService.create(d);
        var p=new vn.iotstar.entity.Product();p.setName("Sản phẩm liên kết");p.setPrice(BigDecimal.ONE);p.setUser(users.findById(saved.getId()).orElseThrow());products.save(p);
        assertThat(userService.countProducts(saved.getId())).isEqualTo(1);userService.delete(saved.getId());assertThat(products.existsById(p.getId())).isFalse();
    }
}
