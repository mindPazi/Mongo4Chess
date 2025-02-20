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

@Service
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {

    private final PlayerService playerService;
    private final PlayerDAO playerDAO;
    private final AdminDAO adminDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerDAO.getPlayer(username);
        //PlayerNode playerNode = playerDAO.getPlayerNode(username);
        if (player != null) {
            playerService.setPlayer(player);
            //playerService.setPlayerNode(playerNode);

            return User.builder()
                    .username(player.getUsername())
                    .password(player.getPassword())
                    //.roles("PLAYER")
                    .build();
        } else if (adminDAO.getAdmin(username) != null) {
            Admin admin = adminDAO.getAdmin(username);
            return User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    //.roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }

}