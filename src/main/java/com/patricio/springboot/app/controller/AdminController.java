package com.patricio.springboot.app.controller;

import com.patricio.springboot.app.dto.UsuarioDTO;
import com.patricio.springboot.app.entity.Usuario;
import com.patricio.springboot.app.mapper.UsuarioMapper;
import com.patricio.springboot.app.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping()
    public ResponseEntity<UsuarioDTO> crearAdmin(@RequestBody UsuarioDTO dto) {
        Usuario admin = adminService.crearAdministrador(dto);
        return ResponseEntity.ok(UsuarioMapper.toDTO(admin));
    }
}