package me.eeshe.tempus.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.repository.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with name: " + username));

        return new UserDetailsImpl(user);
    }

}
