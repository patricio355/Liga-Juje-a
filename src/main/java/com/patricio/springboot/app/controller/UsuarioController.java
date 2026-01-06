package com.patricio.springboot.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @GetMapping("/arbitros")
    public ResponseEntity<List<UsuarioDTO>> getArbitros() {
        return ResponseEntity.ok(usuarioService.listarArbitros());
    }
    // =========================
    // LISTAR USUARIOS
    // =========================
    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // =========================
    // CREAR USUARIO (ADMIN)
    // =========================
    @PostMapping
    public UsuarioDTO crearUsuario(
            @RequestBody CrearUsuarioRequest request
    ) {
        return usuarioService.crearUsuarioAdmin(
                request.getUsuario(),
                request.getPassword()
        );
    }

    // =========================
    // EDITAR USUARIO
    // =========================
    @PutMapping("/{id}")
    public UsuarioDTO editarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioDTO dto
    ) {
        return usuarioService.editarUsuario(id, dto);
    }

    // =========================
    // BAJA LÓGICA
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
