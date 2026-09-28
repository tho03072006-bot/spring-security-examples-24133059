package vn.iotstar.security;

import java.util.*;
import lombok.Getter;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.iotstar.dto.UserDTO;
@Getter
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID=1L;
    private final Long id;
    private final String username,email,password,fullName,images,role;
    private final boolean enabled;
    public CustomUserDetails(UserDTO dto,String password) {
        id=dto.getId(); username=dto.getUsername(); email=dto.getEmail(); this.password=password;
        fullName=dto.getFullName(); images=dto.getImages(); role=dto.getRoleName(); enabled=dto.isEnabled();
    }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority(role)); }
    @Override public boolean isAccountNonExpired(){return true;}
    @Override public boolean isAccountNonLocked(){return true;}
    @Override public boolean isCredentialsNonExpired(){return true;}
    @Override public boolean isEnabled(){return enabled;}
    @Override public boolean equals(Object other){return other instanceof CustomUserDetails u && Objects.equals(id,u.id);}
    @Override public int hashCode(){return Objects.hashCode(id);}
}
