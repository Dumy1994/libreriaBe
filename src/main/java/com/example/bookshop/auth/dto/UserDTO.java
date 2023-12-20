package com.example.bookshop.auth.dto;

import com.example.bookshop.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Set<Role> roles;
    private String nome;
    private String cognome;
    private String indirizzo;
    private String citta;
    private String cap;
    private String telefono;
    private Timestamp dataNascita;
    
}
