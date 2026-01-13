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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoZonaService {

    private final EquipoZonaRepository equipoZonaRepository;
    private final EquipoRepository equipoRepository;
    private final ZonaRepository zonaRepository;
    private final PartidoService partidoService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "torneoDetalle", key = "#result.torneoId", condition = "#result != null"),
            @CacheEvict(value = "zonasPorTorneo", allEntries = true),
            @CacheEvict(value = "dashboardTorneos", allEntries = true)
    })
    public EquipoZonaDTO inscribirEquipo(Long equipoId, Long zonaId) {

        // 1. Usar findByIdOptimized para evitar el problema N+1 al traer la zona y su torneo
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Long torneoId = zona.getTorneo().getId();

        // 2. Validación de duplicados
        // TIP: Si tu torneo está vacío y te sigue dando este error,
        // revisa manualmente la tabla 'equipo_zona' en tu DB, puede haber datos basura.
        boolean yaExiste = equipoZonaRepository
                .existsByNombreEquipoIgnoreCaseAndZona_Torneo_Id(
                        equipo.getNombre(),
                        torneoId
                );

        if (yaExiste) {
            throw new RuntimeException("El equipo '" + equipo.getNombre() + "' ya está inscrito en este torneo.");
        }

        // 3. Crear la relación
        EquipoZona relacion = new EquipoZona();
        relacion.setEquipo(equipo);
        relacion.setZona(zona);
        relacion.setTorneoId(torneoId);
        relacion.setNombreEquipo(equipo.getNombre());

        EquipoZona guardado = equipoZonaRepository.save(relacion);

        return EquipoZonaMapper.toDTO(guardado);
    }




    @Cacheable(value = "equiposPorZona", key = "#zonaId")
    public List<EquipoDTO> listarEquiposPorZona(Long zonaId) {
        return equipoZonaRepository.listarTablaPosiciones(zonaId)
                .stream()
                .map(rel -> EquipoMapper.toDTO(rel.getEquipo()))
                .toList();
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "partidosZona", allEntries = true),
            @CacheEvict(value = "partidosPorZona", allEntries = true),
            @CacheEvict(value = "zonasPorTorneo", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true),
            @CacheEvict(value = "programacion", allEntries = true),
            @CacheEvict(value = "torneos", allEntries = true),
            @CacheEvict(value = "programacionZona", allEntries = true),
            @CacheEvict(value = "fechasDisponibles", allEntries = true),
            @CacheEvict(value = "proximosPartidos", allEntries = true)
    })
    @Transactional
    public void eliminarParticipacion(Long idEquipoZona) {
        // 1. Obtener la participación para conocer el Equipo y la Zona
        EquipoZona participacion = equipoZonaRepository.findById(idEquipoZona)
                .orElseThrow(() -> new RuntimeException("Participación no encontrada"));

        Long equipoId = participacion.getEquipo().getId();
        Long zonaId = participacion.getZona().getId();

        // 2. Delegar a PartidoService la limpieza de partidos y programaciones
        // Pasamos equipoId y zonaId para que solo afecte a esta liga/zona específica
        partidoService.eliminarYRestaurarPartidosPorEquipo(zonaId, equipoId);

        // 3. Finalmente, eliminar la participación (la fila de la tabla de posiciones)
        equipoZonaRepository.delete(participacion);
    }

    public EquipoZonaDTO obtenerEstadisticas(Long id) {
        EquipoZona ez = equipoZonaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participación no encontrada"));


        return EquipoZonaMapper.toDTO(ez);
    }

    @Cacheable(value = "tablaPosiciones", key = "#zonaId")
    public List<EquipoZonaDTO> listarTabla(Long zonaId) {
        return equipoZonaRepository.listarTablaPosiciones(zonaId)
                .stream()
                .map(EquipoZonaMapper::toDTO)
                .toList();
    }


}
