package vn.iotstar.config;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.session.HttpSessionEventPublisher;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean DaoAuthenticationProvider authenticationProvider(UserDetailsService details,PasswordEncoder encoder){
        var provider=new DaoAuthenticationProvider(details); provider.setPasswordEncoder(encoder); return provider;
    }
    @Bean HttpSessionEventPublisher httpSessionEventPublisher(){return new HttpSessionEventPublisher();}
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,DaoAuthenticationProvider provider) throws Exception {
        http.authenticationProvider(provider)
            .authorizeHttpRequests(a->a.requestMatchers("/","/login","/register","/verify-otp","/resend-register-otp","/forgot-password","/reset-password","/css/**","/images/**","/error","/access-denied").permitAll()
                .requestMatchers("/users/**","/admin/**").hasRole("ADMIN").anyRequest().authenticated())
            .formLogin(f->f.loginPage("/login").loginProcessingUrl("/login").usernameParameter("username")
                .defaultSuccessUrl("/",true).failureUrl("/login?error").permitAll())
            .logout(l->l.logoutSuccessUrl("/login?logout").invalidateHttpSession(true).deleteCookies("VD3SESSION"))
            .exceptionHandling(e->e.accessDeniedPage("/access-denied"))
            .sessionManagement(s->s.maximumSessions(1).expiredUrl("/login?expired"));
        return http.build();
    }
}
