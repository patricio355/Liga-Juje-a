package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.repository.EquipoRepository;
import org.springframework.stereotype.Service;
import com.patricio.springboot.app.mapper.EquipoMapper;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class EquipoService {
    private EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }


    public Equipo getEquipoById(Long idEquipo) {
        return equipoRepository.findById(idEquipo).orElseThrow( () -> new RuntimeException("equipo no encontrado"));
    }

    public List<EquipoDTO> listarEquipos() {
        return equipoRepository.findAll()
                .stream()
                .map(EquipoMapper::toDTO)
                .toList();
    }
}
