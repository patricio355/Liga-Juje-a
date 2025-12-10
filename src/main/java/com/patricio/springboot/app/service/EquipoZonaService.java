package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.dto.EquipoZonaDTO;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.EquipoZona;
import com.patricio.springboot.app.entity.Zona;
import com.patricio.springboot.app.mapper.EquipoMapper;
import com.patricio.springboot.app.mapper.EquipoZonaMapper;
import com.patricio.springboot.app.repository.EquipoRepository;
import com.patricio.springboot.app.repository.EquipoZonaRepository;
import com.patricio.springboot.app.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoZonaService {

    private final EquipoZonaRepository equipoZonaRepository;
    private final EquipoRepository equipoRepository;
    private final ZonaRepository zonaRepository;

    public EquipoZonaDTO inscribirEquipo(Long equipoId, Long zonaId) {

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        EquipoZona relacion = new EquipoZona();
        relacion.setEquipo(equipo);
        relacion.setZona(zona);
        relacion.setNombreEquipo(equipo.getNombre());

        EquipoZona guardado = equipoZonaRepository.save(relacion);

        return EquipoZonaMapper.toDTO(guardado);
    }

    public List<EquipoDTO> listarEquiposPorZona(Long zonaId) {
        return equipoZonaRepository.listarTablaPosiciones(zonaId)
                .stream()
                .map(rel -> EquipoMapper.toDTO(rel.getEquipo()))
                .toList();
    }

    public void eliminarParticipacion(Long id) {
        equipoZonaRepository.deleteById(id);
    }

    public EquipoZonaDTO obtenerEstadisticas(Long id) {
        EquipoZona ez = equipoZonaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participación no encontrada"));


        return EquipoZonaMapper.toDTO(ez);
    }

    public List<EquipoZonaDTO> listarTabla(Long zonaId) {
        return equipoZonaRepository.listarTablaPosiciones(zonaId)
                .stream()
                .map(EquipoZonaMapper::toDTO)
                .toList();
    }

    public void actualizarStats(EquipoZona ez, int golesHechos, int golesRecibidos) {

        ez.setPartidosJugados(ez.getPartidosJugados() + 1);
        ez.setGolesAFavor(ez.getGolesAFavor() + golesHechos);
        ez.setGolesEnContra(ez.getGolesEnContra() + golesRecibidos);

        if (golesHechos > golesRecibidos) {
            ez.setGanados(ez.getGanados() + 1);
            ez.setPuntos(ez.getPuntos() + 3);

        } else if (golesHechos == golesRecibidos) {
            ez.setEmpatados(ez.getEmpatados() + 1);
            ez.setPuntos(ez.getPuntos() + 1);

        } else {
            ez.setPerdidos(ez.getPerdidos() + 1);
        }
    }
}
