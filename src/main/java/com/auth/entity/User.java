package com.auth.entity;

import java.util.Date;
import java.util.UUID;

import com.auth.dto.UserDto;
import com.auth.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

	@Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String email;

    private String password;

    private String name;

    private UserRole role;
    private String societe;  

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();
    
    @Lob
    @Column(columnDefinition = "longblob")
    private byte[] img;


    public UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);
        userDto.setImg(img);
        userDto.setUserRole(role);
        userDto.setCreationDate(creationDate);
        userDto.setSociete(societe);
        return userDto;
    }

}
