package vn.iotstar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.UserRepository;

// Ví dụ 2: chuỗi nhập vào được so khớp với cả username lẫn email
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        String value = login.trim();
        var user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(value, value)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy username/email: " + value));
        return new CustomUserDetails(userMapper.toDTO(user), user.getPassword());
    }
}
