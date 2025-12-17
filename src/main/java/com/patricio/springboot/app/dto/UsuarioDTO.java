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
    private String email;
    private String dni;
    private String telefono;
    private String domicilio;
    private String rol;
    private Boolean activo;
}
