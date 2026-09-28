package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.*;
import vn.iotstar.repository.*;
@Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.seed:true}") private boolean seed;
    @Override @Transactional public void run(String... args){
        var userRole=roles.findByName("ROLE_USER").orElseGet(()->roles.save(new Role("ROLE_USER")));
        var adminRole=roles.findByName("ROLE_ADMIN").orElseGet(()->roles.save(new Role("ROLE_ADMIN")));
        if(seed){add("admin","admin@iotstar.vn","Quản trị viên",adminRole,true);add("user01","user01@gmail.com","Nguyễn Hữu Trung",userRole,true);add("pending","pending@iotstar.vn","Tài khoản chưa kích hoạt",userRole,false);}
    }
    private void add(String username,String email,String name,Role role,boolean enabled){
        if(users.existsByUsernameIgnoreCase(username))return;
        User u=new User();u.setUsername(username);u.setEmail(email);u.setFullName(name);u.setRole(role);
        u.setPassword(encoder.encode("123456"));u.setEnabled(enabled);u.setImages("/images/avatar.svg");users.save(u);
    }
}
