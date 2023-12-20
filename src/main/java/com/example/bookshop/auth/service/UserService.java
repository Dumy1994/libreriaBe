package com.example.bookshop.auth.service;

import com.example.bookshop.auth.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService{
    public User save(User user);
    public List<User> getUsers();
    public User findByUsername(String username);
    public User findByEmail(String email);
    public User getAdmin();
    public User getUser(Long idUser);
    public List<User> getAllUsersValid(boolean valid);

}
