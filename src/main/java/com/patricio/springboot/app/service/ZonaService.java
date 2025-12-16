package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Zona;
import com.patricio.springboot.app.mapper.ZonaMapper;
import com.patricio.springboot.app.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaService {

    private final ZonaRepository zonaRepository;


    public List<ZonaDTO> listarPorTorneo(Long torneoId) {
        return zonaRepository.findByTorneoId(torneoId)
                .stream()
                .map(ZonaMapper::toDTO)
                .toList();
    }



    /**
     * Edita una zona existente.
     * @param id ID de la zona
     * @param dto Datos nuevos
     * @return ZonaDTO actualizado
     */
    public ZonaDTO editarZona(Long id, ZonaDTO dto) {

        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        zona.setNombre(dto.getNombre());
        zona.setDescripcion(dto.getDescripcion());

        Zona actualizada = zonaRepository.save(zona);

        return ZonaMapper.toDTO(actualizada);
    }
}
