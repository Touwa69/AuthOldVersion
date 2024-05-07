package com.auth.dto;

import java.util.Date;
import java.util.UUID;

import com.auth.enums.UserRole;
import lombok.Data;

@Data
public class UserDto {

    private UUID id;
    private String email;
    private String name;
    private UserRole userRole;
    private byte[] img;
    private Date creationDate;
    private String societe;
}
