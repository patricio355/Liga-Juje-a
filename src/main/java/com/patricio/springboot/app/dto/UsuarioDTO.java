package com.patricio.springboot.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String contrasenia;
    private String email;
    private String telefono;
    private String dni;
    private String domicilio;
    private String rol;
}
