package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.CanchaDTO;
import com.patricio.springboot.app.entity.Cancha;
import com.patricio.springboot.app.entity.Partido;
import com.patricio.springboot.app.entity.ProgramacionFecha;
import com.patricio.springboot.app.mapper.CanchaMapper;
import com.patricio.springboot.app.repository.CanchaRepository;
import com.patricio.springboot.app.repository.PartidoRepository;
import com.patricio.springboot.app.repository.ProgramacionFechaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.patricio.springboot.app.entity.Usuario; // Asumo que tienes esta entidad
import com.patricio.springboot.app.repository.UsuarioRepository; // Necesario para obtener el ID del creador
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
@Service
public class CanchaService {

    private final CanchaRepository canchaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PartidoRepository partidoRepository;
    private final ProgramacionFechaRepository programacionRepo;

    public CanchaService(CanchaRepository canchaRepository, UsuarioRepository usuarioRepository, PartidoRepository partidoRepository, ProgramacionFechaRepository programacionRepo) {
        this.canchaRepository = canchaRepository;
        this.usuarioRepository = usuarioRepository;
        this.partidoRepository = partidoRepository;
        this.programacionRepo = programacionRepo;
    }

    public Optional<CanchaDTO> buscarForID(Long id) {
        return canchaRepository.findById(id).map(CanchaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CanchaDTO> listarCanchas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // Si es ADMIN, traemos todas las que no estén borradas lógicamente
            return canchaRepository.findAllByEstadoTrue().stream()
                    .map(CanchaMapper::toDTO)
                    .toList();
        } else {
            // Si no es ADMIN, filtramos por el email del creador (suponiendo que tienes esta relación)
            return canchaRepository.findByCreadorEmailAndEstadoTrue(email).stream()
                    .map(CanchaMapper::toDTO)
                    .toList();
        }
    }

    @Transactional
    public CanchaDTO crearCancha(CanchaDTO dto) {
        // 1. Obtenemos el creador desde el contexto de seguridad
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario creador = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado"));

        // 2. VALIDACIÓN DE DUPLICADOS: Comprobar si este creador ya tiene una cancha con ese nombre
        if (dto.getNombre() != null) {

            String nombreNormalized = dto.getNombre().trim().toUpperCase();
            boolean yaExiste = canchaRepository
                    .findByNombreAndCreadorId(nombreNormalized, creador.getId())
                    .isPresent();

            if (yaExiste) {
                throw new RuntimeException("Ya tienes registrada una cancha con el nombre: " + nombreNormalized);
            }
        } else {
            throw new RuntimeException("El nombre de la cancha es obligatorio");
        }

        // 3. Mapeo y asignación de valores
        Cancha cancha = CanchaMapper.toEntity(dto);
        cancha.setNombre(dto.getNombre().trim().toUpperCase());
        cancha.setCreador(creador);

        // Manejo seguro del estado (evitando NPE)
        cancha.setEstado(dto.getEstado() != null ? dto.getEstado() : true);

        // 4. Persistencia
        Cancha guardado = canchaRepository.save(cancha);
        return CanchaMapper.toDTO(guardado);
    }

    @Transactional
    public void eliminarCancha(Long id) {
        // 1. Buscamos la cancha
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la cancha"));

        // 2. Limpiar referencias en Partidos (o tablas similares)
        // Suponiendo que tienes un partidoRepository
        List<Partido> partidosAsociados = partidoRepository.findByCanchaId(id);
        partidosAsociados.forEach(p -> p.setCancha(null));
        partidoRepository.saveAll(partidosAsociados);

        // 3. Limpiar referencias en ProgramacionFecha
        List<ProgramacionFecha> programaciones = programacionRepo.findByCanchaId(id);
        programaciones.forEach(pf -> pf.setCancha(null));
        programacionRepo.saveAll(programaciones);

        // 4. Eliminación física
        canchaRepository.delete(cancha);

    }

    @Transactional
    public CanchaDTO actualizarCancha(Long id, CanchaDTO dto) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cancha no encontrada con ID: " + id));

        if (!cancha.getNombre().equalsIgnoreCase(dto.getNombre())) {

            // Buscamos si existe otra cancha con ese nombre para el MISMO creador
            boolean nombreYaExiste = canchaRepository
                    .findByNombreAndCreadorId(dto.getNombre().toUpperCase(), cancha.getCreador().getId())
                    .isPresent();

            if (nombreYaExiste) {
                throw new RuntimeException("Ya existe una cancha con el nombre: " + dto.getNombre().toUpperCase());
            }
        }
        // Actualizamos campos básicos
        cancha.setNombre(dto.getNombre());
        cancha.setUbicacion(dto.getUbicacion());
        cancha.setFotoUrl(dto.getFotoUrl());
        cancha.setValorEntrada(dto.getValorEntrada());
        cancha.setUbicacionUrl(dto.getUbicacionUrl());

        // Agrega aquí otros campos que necesites como latitud, longitud, etc.

        Cancha actualizado = canchaRepository.save(cancha);
        return CanchaMapper.toDTO(actualizado);
    }
}