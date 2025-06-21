package com.ns.iestpffaaarchives.infrastructure.security;

import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null && user.getRole().getName() != null) {
            // Añadir el rol como autoridad
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
            
            // Añadir los permisos individuales del rol como autoridades
            if (user.getRole().getPermissions() != null) {
                for (Permission permission : user.getRole().getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);
    }
}
