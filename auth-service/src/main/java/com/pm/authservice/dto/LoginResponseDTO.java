package com.pm.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class LoginResponseDTO implements Serializable {

    private final String token;

}
