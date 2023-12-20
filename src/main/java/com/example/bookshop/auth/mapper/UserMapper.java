package com.example.bookshop.auth.mapper;

import com.example.bookshop.auth.dto.UserDTO;
import com.example.bookshop.auth.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {



    public abstract UserDTO toDto(User entity);

    public abstract User toEntity(UserDTO dto);

    public List<UserDTO> toDTOfromList(List<User> e) {
        return e.stream().map(this::toDto).collect(Collectors.toList());
    }
}
