package com.patricio.springboot.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Partidos", description = "Operaciones relacionadas con los partidos")
@RestController
@RequestMapping("/api/partidos")
public class PartidoController {
}
