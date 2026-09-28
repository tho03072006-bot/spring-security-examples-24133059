package vn.iotstar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class LoginIntegrationTest {
    @Autowired MockMvc mvc;
    @Test void publicPagesRender() throws Exception {mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("IOTSTAR SHOP")));mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("_csrf")));}
    @Test void validLoginCreatesSessionAndHeader() throws Exception {
        var result=mvc.perform(formLogin().userParameter("login").user("admin").password("123456")).andExpect(authenticated().withUsername("admin")).andExpect(redirectedUrl("/")).andReturn();
        var session=(MockHttpSession)result.getRequest().getSession(false);
        mvc.perform(get("/").session(session)).andExpect(status().isOk()).andExpect(content().string(containsString("Quản trị viên"))).andExpect(content().string(containsString("admin@iotstar.vn"))).andExpect(content().string(containsString("avatar.svg")));
    }
    @Test void wrongPasswordRejected() throws Exception {mvc.perform(formLogin().userParameter("login").user("admin").password("wrong")).andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));}
    @Test void disabledAccountRejected() throws Exception {mvc.perform(formLogin().userParameter("login").user("pending").password("123456")).andExpect(unauthenticated());}
    @Test void csrfRequired() throws Exception {mvc.perform(post("/login").param("login","admin").param("password","123456")).andExpect(status().isForbidden());}
    @Test void anonymousRedirected() throws Exception {mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());}
    @Test void userCannotVisitAdminPage() throws Exception {
        var r=mvc.perform(formLogin().userParameter("login").user("user01").password("123456")).andExpect(authenticated()).andReturn();
        mvc.perform(get("/admin").session((MockHttpSession)r.getRequest().getSession(false))).andExpect(status().isForbidden());
    }
    @Test void logoutInvalidatesSession() throws Exception {
        var r=mvc.perform(formLogin().userParameter("login").user("admin").password("123456")).andReturn();
        mvc.perform(post("/logout").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()).session((MockHttpSession)r.getRequest().getSession(false))).andExpect(unauthenticated()).andExpect(redirectedUrl("/login?logout"));
    }
    @Test void emailAlsoLogsIn() throws Exception {mvc.perform(formLogin().userParameter("login").user("USER01@GMAIL.COM").password("123456")).andExpect(authenticated().withUsername("user01"));}
}
