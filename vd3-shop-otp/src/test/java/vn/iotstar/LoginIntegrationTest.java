package vn.iotstar;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired
    MockMvc mvc;

    // Đăng nhập bằng username, trả về session để dùng tiếp
    private MockHttpSession login(String username) throws Exception {
        var result = mvc.perform(formLogin().user(username).password("123456"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    void loginScriptLoadsWithoutAuthentication() throws Exception {
        mvc.perform(get("/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"))
                .andExpect(content().string(containsString("data-password-toggle")));
    }

    @Test
    void publicPagesRender() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("IOTSTAR SHOP")));
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    void validLoginShowsUserOnHeader() throws Exception {
        mvc.perform(formLogin().user("admin").password("123456"))
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/").session(login("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Quản trị viên")))
                .andExpect(content().string(containsString("admin@iotstar.vn")))
                .andExpect(content().string(containsString("avatar.svg")));
    }

    @Test
    void wrongPasswordRejected() throws Exception {
        mvc.perform(formLogin().user("admin").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void notActivatedAccountRejected() throws Exception {
        mvc.perform(formLogin().user("pending").password("123456"))
                .andExpect(unauthenticated());
    }

    @Test
    void csrfRequired() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", "123456"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousRedirectedToLogin() throws Exception {
        mvc.perform(get("/products")).andExpect(status().is3xxRedirection());
    }

    @Test
    void userCannotManageUsers() throws Exception {
        mvc.perform(get("/users").session(login("user01")))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        mvc.perform(post("/logout").with(csrf()).session(login("admin")))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
