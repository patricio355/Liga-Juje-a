package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.UsuarioDTO;
import lombok.Data;

@Data
public class CrearUsuarioRequest {
    private UsuarioDTO usuario;
    private String password;
}