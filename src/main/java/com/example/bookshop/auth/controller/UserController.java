package com.example.bookshop.auth.controller;

import com.example.bookshop.auth.dto.UserDTO;
import com.example.bookshop.auth.mapper.UserMapper;
import com.example.bookshop.auth.model.User;
import com.example.bookshop.auth.service.UserDetailsServiceImpl;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log
@RequestMapping("/api/users")
@CrossOrigin("*")
@RestController
public class UserController {
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    UserMapper mapper;

    @GetMapping()
    public List<UserDTO> getAllUsers() {
        return mapper.toDTOfromList(userDetailsService.getUser());
    }


    // delete 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") long id) {
        userDetailsService.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> UpdateUser(@RequestBody User userData) {
        User userUpdate = userDetailsService.findById(userData.getId());
        if (userUpdate == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userUpdate.setUsername(userData.getUsername());
        userUpdate.setEmail(userData.getEmail());
        //userUpdate.setRoles(userData.getRoles());
        userUpdate.setNome(userData.getNome());
        userUpdate.setCognome(userData.getCognome());
        userUpdate.setIndirizzo(userData.getIndirizzo());
        userUpdate.setCitta(userData.getCitta());
        userUpdate.setCap(userData.getCap());
        userUpdate.setTelefono(userData.getTelefono());
        userUpdate.setDataNascita(userData.getDataNascita());
        userDetailsService.saveUser(userUpdate);
        return new ResponseEntity<User>((userUpdate), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public UserDTO getViewUser(@PathVariable("id") long id) {
        return mapper.toDto(userDetailsService.findById(id));
    }


    //@GetMapping("/{id}")
    //@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    //public ResponseEntity<?> getUserById(@PathVariable("id") long id) {
        //User user = userDetailsService.findById(id);
        //if (user == null) {
         //   return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       // }
     //   return new ResponseEntity<User>(user, HttpStatus.OK);
   // }

}

