package vn.iotstar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.*;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.mapper.UserMapper;
@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    private final UserMapper mapper;
    @Override @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String login) {
        var u=users.findByEmailIgnoreCase(login.trim()).orElseThrow(()->new UsernameNotFoundException("Không tìm thấy tài khoản"));
        return new CustomUserDetails(mapper.toDTO(u),u.getPassword());
    }
}
