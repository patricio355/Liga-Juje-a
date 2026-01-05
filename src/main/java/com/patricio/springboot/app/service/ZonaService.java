package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Zona;
import com.patricio.springboot.app.mapper.ZonaMapper;
import com.patricio.springboot.app.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaService {

    private final ZonaRepository zonaRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "zonasPorTorneo", key = "#torneoId")
    public List<ZonaDTO> listarPorTorneo(Long torneoId) {
        // Usamos el método optimizado para traer equipos de una vez
        return zonaRepository.findByTorneoIdOptimized(torneoId)
                .stream()
                .map(ZonaMapper::toDTO)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "zonasPorTorneo", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "dashboardTorneos", allEntries = true)
    })
    public ZonaDTO editarZona(Long id, ZonaDTO dto) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        zona.setNombre(dto.getNombre());
        zona.setDescripcion(dto.getDescripcion());

        Zona actualizada = zonaRepository.save(zona);
        return ZonaMapper.toDTO(actualizada);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "zonaDetalle", key = "#id")
    public ZonaDTO obtenerPorId(Long id) {
        // Buscamos con JOIN FETCH para que el DTO tenga todo rápido
        Zona zona = zonaRepository.findByIdOptimized(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con ID: " + id));

        return ZonaMapper.toDTO(zona);
    }
}