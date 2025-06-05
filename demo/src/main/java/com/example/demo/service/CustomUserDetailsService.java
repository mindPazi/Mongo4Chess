package com.example.demo.service;

import com.example.demo.dao.PlayerDAO;
import com.example.demo.dao.AdminDAO;
import lombok.RequiredArgsConstructor;
import com.example.demo.model.Player;
import com.example.demo.model.Admin;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService is a service that implements UserDetailsService to provide
 * custom user authentication logic for Spring Security.
 */
@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PlayerService playerService;
    private final PlayerDAO playerDAO;
    private final AdminDAO adminDAO;

    /**
     * Loads the user by username. This method is used by Spring Security to
     * authenticate a user.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated UserDetails object (never null).
     * @throws UsernameNotFoundException if the user could not be found or the user has no GrantedAuthority.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerDAO.getPlayer(username);
        if (player != null) {
            if(player.getIsBanned())
                throw new UsernameNotFoundException("You are banned");
            return User.builder()
                    .username(player.getUsername())
                    .password(player.getPassword())
                    .roles("PLAYER")
                    .build();
        } else if (adminDAO.getAdmin(username) != null) {
            Admin admin = adminDAO.getAdmin(username);
            return User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }

}