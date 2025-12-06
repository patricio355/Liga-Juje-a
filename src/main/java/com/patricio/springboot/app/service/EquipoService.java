package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.CanchaDTO;
import com.patricio.springboot.app.dto.EquipoDTO;
import com.patricio.springboot.app.dto.ZonaDTO;
import com.patricio.springboot.app.entity.Cancha;
import com.patricio.springboot.app.entity.Encargado;
import com.patricio.springboot.app.entity.Equipo;
import com.patricio.springboot.app.entity.Zona;
import com.patricio.springboot.app.repository.CanchaRepository;
import com.patricio.springboot.app.repository.EquipoRepository;
import com.patricio.springboot.app.repository.ZonaRepository;
import org.springframework.stereotype.Service;
import com.patricio.springboot.app.mapper.EquipoMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class EquipoService {
    private EquipoRepository equipoRepository;
    private ZonaRepository zonaRepository;
    private CanchaRepository canchaRepository;

    public EquipoService(EquipoRepository equipoRepository , ZonaRepository zonaRepository, CanchaRepository canchaRepository) {
        this.equipoRepository = equipoRepository;
        this.zonaRepository = zonaRepository;
        this.canchaRepository = canchaRepository;
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

    public List<EquipoDTO> listarEquiposActivos() {
        return equipoRepository.findAllByEstado(true)
                .stream()
                .map(EquipoMapper::toDTO)
                .toList();
    }


    public EquipoDTO crearEquipo(EquipoDTO dto) {

        Equipo equipo = EquipoMapper.toEntity(dto);
        equipo.setFechaCreacion(LocalDate.now());
        // ZONA
        if (dto.getZonaId() != null) {
            Zona zona = zonaRepository.findById(dto.getZonaId())
                    .orElseThrow(() -> new RuntimeException("Zona inexistente"));
            equipo.setZona(zona);
        }

        // CANCHA
        if (dto.getCanchaId() != null) {
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new RuntimeException("Cancha inexistente"));
            equipo.setLocalia(cancha);
        }

        // ENCARGADO
//        if (dto.getEncargadoId() != null) {
//            Encargado encargado = encargadoRepository.findById(dto.getEncargadoId())
//                    .orElseThrow(() -> new RuntimeException("Encargado inexistente"));
            equipo.setEncargado(null);
//        }



        Equipo guardado = equipoRepository.save(equipo);

        return EquipoMapper.toDTO(guardado);
    }

    public EquipoDTO eliminarEquipo(Long id) {
        Equipo eq = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no existe"));

        eq.setEstado(false);
        equipoRepository.save(eq);
        return EquipoMapper.toDTO(eq);
    }

    public EquipoDTO editarEquipo(Long id, EquipoDTO dto) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no existe"));

        equipo.setNombre(dto.getNombre());
        equipo.setCamisetaSuplente(dto.getCamisetaSuplente());
        equipo.setCamisetaTitular(dto.getCamisetaTitular());
        equipo.setEscudo(dto.getEscudo());
        equipo.setZona(equipo.getZona());
        equipo.setLocalia(equipo.getLocalia());
        equipo.setEncargado(equipo.getEncargado());
        equipoRepository.save(equipo);
        return EquipoMapper.toDTO(equipo);
    }

    public void asignarCancha(CanchaDTO dto) {

    }

    public void asignarZona(ZonaDTO dto) {

    }

    public void asignarEntrenador(){

    }

}
