package com.patricio.springboot.app.service;

import com.patricio.springboot.app.dto.FixtureFechaDTO;
import com.patricio.springboot.app.dto.PartidoCreateDTO;
import com.patricio.springboot.app.entity.*;
import com.patricio.springboot.app.mapper.PartidoMapper;
import com.patricio.springboot.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartidoService {

    private final EquipoZonaRepository equipoZonaRepository;
    private final PartidoRepository partidoRepository;
    private final EquipoRepository equipoRepository;
    private final CanchaRepository canchaRepository;
    private final ZonaRepository zonaRepository;
    private final SolicitudCierrePartidoRepository solicitudRepo;
    private final ProgramacionFechaRepository programacionRepo;
    private final UsuarioRepository usuarioRepository;
    private final ProgramacionFechaService programacionService;
    private final EtapaTorneoRepository etapaTorneoRepository;
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "partidosPorZona", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "faseFinalData", allEntries = true)
    })
    public Partido crearPartido(PartidoCreateDTO dto) {

        // 1. VALIDACIÓN FLEXIBLE DE INSCRIPCIÓN
        // Solo validamos si es fase de grupos (etapaId == null) Y si los equipos están presentes
        if (dto.getEtapaId() == null && dto.getEquipoLocalId() != null && dto.getEquipoVisitanteId() != null) {
            if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoLocalId(), dto.getZonaId()) == null) {
                throw new RuntimeException("El equipo local NO está inscripto en la zona.");
            }
            if (equipoZonaRepository.findByEquipoIdAndZonaId(dto.getEquipoVisitanteId(), dto.getZonaId()) == null) {
                throw new RuntimeException("El equipo visitante NO está inscripto en la zona.");
            }
        }

        Partido partido = new Partido();

        // 2. BUSQUEDA OPCIONAL DE EQUIPOS
        if (dto.getEquipoLocalId() != null) {
            partido.setEquipoLocal(equipoRepository.findById(dto.getEquipoLocalId())
                    .orElseThrow(() -> new RuntimeException("Equipo local no encontrado")));
        }

        if (dto.getEquipoVisitanteId() != null) {
            partido.setEquipoVisitante(equipoRepository.findById(dto.getEquipoVisitanteId())
                    .orElseThrow(() -> new RuntimeException("Equipo visitante no encontrado")));
        }

        // 3. BUSQUEDA OPCIONAL DE CANCHA
        if (dto.getCanchaId() != null) {
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new RuntimeException("Cancha no encontrada"));
            partido.setCancha(cancha);
        }

        // --- MANEJO DE ZONA ---
        Zona zona = null;
        if (dto.getZonaId() != null && dto.getZonaId() > 0) {
            zona = zonaRepository.findById(dto.getZonaId())
                    .orElseThrow(() -> new RuntimeException("Zona no encontrada"));
            partido.setZona(zona);
        }

        // --- MANEJO DE ETAPA (Obligatorio para fase final) ---
        if (dto.getEtapaId() != null) {
            EtapaTorneo etapa = etapaTorneoRepository.findById(dto.getEtapaId())
                    .orElseThrow(() -> new RuntimeException("Etapa no encontrada"));
            partido.setEtapa(etapa);
            // Seteamos el orden para que el Cuadro sepa dónde ubicarlo
            partido.setOrden(dto.getOrden());
        }

        // Mapeo de Fecha, Hora y resto de campos
        if (dto.getFecha() != null ) {
            partido.setFecha((dto.getFecha()));
        }
        if (dto.getHora() != null ) {
            partido.setHora((dto.getHora()));
        }

        partido.setVeedor(dto.getVeedor());
        partido.setEstado("PENDIENTE");
        partido.setNumeroFecha(dto.getNumeroFecha() != null ? dto.getNumeroFecha() : 1);

        Partido partidoGuardado = partidoRepository.save(partido);

        // 4. PROGRAMACIÓN (Calendario)
        // Solo creamos programación si tenemos fecha, hora y cancha
        if (partidoGuardado.getFecha() != null && partidoGuardado.getCancha() != null) {
            ProgramacionFecha pf = new ProgramacionFecha();
            pf.setZona(zona);
            pf.setPartido(partidoGuardado);
            pf.setNumeroFecha(partidoGuardado.getNumeroFecha());
            pf.setFecha(partidoGuardado.getFecha());
            pf.setHora(partidoGuardado.getHora());
            pf.setCancha(partidoGuardado.getCancha());
            pf.setEstado("PROGRAMADO");
            programacionRepo.save(pf);
        }

        return partidoGuardado;
    }

    @Transactional
    public void eliminarPartidoFaseFinal(Long partidoId) {
        // 1. Verificar existencia del partido
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el partido con ID: " + partidoId));

        programacionService.eliminarProgramacionPorPartido(partidoId);
        // 5. Finalmente, eliminar el partido
        partidoRepository.delete(partido);
    }

    @Transactional
    public Partido actualizarPartido(Long id, PartidoCreateDTO dto) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Partido no encontrado con ID: " + id));

        // Actualizar Equipos (Buscando las entidades por ID)
        if (dto.getEquipoLocalId() != null) {
            Equipo local = equipoRepository.findById(dto.getEquipoLocalId())
                    .orElseThrow(() -> new EntityNotFoundException("Equipo local no encontrado"));
            partido.setEquipoLocal(local);
        } else {
            partido.setEquipoLocal(null);
        }

        if (dto.getEquipoVisitanteId() != null) {
            Equipo visitante = equipoRepository.findById(dto.getEquipoVisitanteId())
                    .orElseThrow(() -> new EntityNotFoundException("Equipo visitante no encontrado"));
            partido.setEquipoVisitante(visitante);
        } else {
            partido.setEquipoVisitante(null);
        }

        // Actualizar Cancha
        if (dto.getCanchaId() != null) {
            Cancha cancha = canchaRepository.findById(dto.getCanchaId())
                    .orElseThrow(() -> new EntityNotFoundException("Cancha no encontrada"));
            partido.setCancha(cancha);
        } else {
            partido.setCancha(null);
        }

        // Actualizar datos básicos
        partido.setFecha(dto.getFecha());
        partido.setHora(dto.getHora());
        partido.setVeedor(dto.getVeedor());


        // El orden es importante para el cuadro de fase final
        if (dto.getOrden() != null) {
            partido.setOrden(dto.getOrden());
        }

        return partidoRepository.save(partido);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "partidosPorZona", allEntries = true),
            @CacheEvict(value = "torneoDetalle", allEntries = true),
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "faseFinalData", allEntries = true),
            @CacheEvict(value = "programacion", allEntries = true),
            @CacheEvict(value = "torneos", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true), // Si tenés cacheada la tabla
            @CacheEvict(value = "opcionesProgramacion", allEntries = true), // ¡ESTE ES CLAVE!
            @CacheEvict(value = "fixture", allEntries = true),


    })
    public void eliminarPartidoCompleto(Long partidoId) {
        // 1. Buscar el partido
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        // 2. REVERTIR TABLA DE POSICIONES (equipo_zona)
        // Necesitas buscar las entradas de equipo_zona para el local y visitante
        // Asumiendo que tienes el torneo_id o zona_id en el partido
        if ("FINALIZADO".equals(partido.getEstado())) {
            revertirEstadisticasEquipo(partido.getEquipoLocal(), partido.getGolesLocal(), partido.getGolesVisitante(), partido.getZona().getTorneo().getId());
            revertirEstadisticasEquipo(partido.getEquipoVisitante(), partido.getGolesVisitante(), partido.getGolesLocal(), partido.getZona().getTorneo().getId());
        }
        // 3. ELIMINAR REGISTROS ASOCIADOS


        programacionRepo.deleteByPartidoId(partidoId);

        // 4. ELIMINACIÓN FÍSICA DEL PARTIDO
        partido.setGanador(null);
        partido.setFecha(null);
        partido.setEstado("PENDIENTE");
        partido.setHora(null);
        partido.setVeedor(null);
        partido.setArbitro(null);
        partido.setCancha(null);
        partido.setGolesLocal(null);
        partido.setGolesVisitante(null);
        partido.setOrden(null);
        partido.setEtapa(null);

        partidoRepository.save(partido);

    }

    private void revertirEstadisticasEquipo(Equipo equipo, int golesFavor, int golesContra, Long torneoId) {
        // Buscamos la fila en la tabla equipo_zona (la que mostraste en image_226752.png)
        EquipoZona ez = equipoZonaRepository.findByEquipoAndTorneoId(equipo, torneoId);

        if (ez != null) {
            ez.setPartidosJugados(ez.getPartidosJugados() - 1);
            ez.setGolesAFavor(ez.getGolesAFavor() - golesFavor);
            ez.setGolesEnContra(ez.getGolesEnContra() - golesContra);

            if (golesFavor > golesContra) { // Ganó
                ez.setGanados(ez.getGanados() - 1);
                ez.setPuntos(ez.getPuntos() - ez.getZona().getTorneo().getPuntosGanador());
            } else if (golesFavor < golesContra) { // Perdió
                ez.setPerdidos(ez.getPerdidos() - 1);
            } else { // Empató
                ez.setEmpatados(ez.getEmpatados() - 1);
                ez.setPuntos(ez.getPuntos() - ez.getZona().getTorneo().getPuntosEmpate());
            }
            equipoZonaRepository.save(ez);
        }
    }



    @Transactional
    public Partido cerrarPartidoFaseFinal(Long partidoId, Integer golesLocal, Integer golesVisitante, Integer penalesLocal, Integer penalesVisitante) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new EntityNotFoundException("Partido no encontrado"));

        // 1. Actualizar resultado reglamentario
        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);

        // 2. Actualizar resultado de penales (asegúrate de tener estos campos en tu entidad Partido)
        partido.setGolesLocalPenales(penalesLocal != null ? penalesLocal : 0);
        partido.setGolesVisitantePenales(penalesVisitante != null ? penalesVisitante : 0);

        partido.setEstado("FINALIZADO");

        // 3. Determinar Ganador contemplando penales
        if (golesLocal > golesVisitante) {
            // Ganador en tiempo reglamentario
            partido.setGanador(partido.getEquipoLocal());
        } else if (golesVisitante > golesLocal) {
            // Ganador en tiempo reglamentario
            partido.setGanador(partido.getEquipoVisitante());
        } else {
            // Empate en tiempo reglamentario: definir por penales
            if (penalesLocal == null || penalesVisitante == null || penalesLocal.equals(penalesVisitante)) {
                throw new IllegalStateException("En fase final debe haber un ganador (revisar penales).");
            }

            if (penalesLocal > penalesVisitante) {
                partido.setGanador(partido.getEquipoLocal());
            } else {
                partido.setGanador(partido.getEquipoVisitante());
            }
        }

        // 4. Lógica de Torneo (Null Safe)
        String nombreTorneo = "";
        if (partido.getZona() != null && partido.getZona().getTorneo() != null) {
            nombreTorneo = partido.getZona().getTorneo().getNombre();
        } else if (partido.getEtapa() != null && partido.getEtapa().getTorneo() != null) {
            nombreTorneo = partido.getEtapa().getTorneo().getNombre();
        }

        System.out.println("Procesando fin de partido para el torneo: " + nombreTorneo);

        return partidoRepository.save(partido);
    }


    public List<FixtureFechaDTO> obtenerFixturePorZona(Long zonaId) {

        List<Partido> partidos =
                partidoRepository.findByZonaIdOrderByNumeroFechaAscFechaAscHoraAsc(zonaId);

        return partidos.stream()
                .collect(Collectors.groupingBy(
                        Partido::getNumeroFecha,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> new FixtureFechaDTO(
                        entry.getKey(),
                        entry.getValue()
                                .stream()
                                .map(PartidoMapper::toDTO)
                                .toList()
                ))
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "torneoDetalle", allEntries = true), // Limpia todos los detalles
            @CacheEvict(value = "dashboardTorneos", allEntries = true),
            @CacheEvict(value = "torneosActivos", allEntries = true)
    })
    public void generarFixtureInicialZona(Long zonaId) {

        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        List<Equipo> equipos = zona.getEquiposZona()
                .stream()
                .map(EquipoZona::getEquipo)
                .distinct()
                .toList();

        if (equipos.size() < 2) return;

        if (partidoRepository.existsByZonaId(zonaId)) {
            throw new RuntimeException("El fixture ya fue generado");
        }

        List<Equipo> lista = new ArrayList<>(equipos);
        boolean impar = lista.size() % 2 != 0;
        if (impar) lista.add(null);

        int n = lista.size();
        int rondas = n - 1;

        for (int ronda = 0; ronda < rondas; ronda++) {
            int numeroFecha = ronda + 1;

            for (int i = 0; i < n / 2; i++) {
                Equipo a = lista.get(i);
                Equipo b = lista.get(n - 1 - i);

                if (a == null || b == null) continue;

                Equipo local = (ronda % 2 == 0) ? a : b;
                Equipo visitante = (ronda % 2 == 0) ? b : a;

                // 1. Crear y guardar el Partido
                Partido p = new Partido();
                p.setZona(zona);
                p.setEquipoLocal(local);
                p.setEquipoVisitante(visitante);
                p.setNumeroFecha(numeroFecha);
                p.setEstado("PENDIENTE");


                Partido partidoGuardado = partidoRepository.save(p);

                // 2. Crear y guardar la Programación Automática
                ProgramacionFecha pf = new ProgramacionFecha();
                pf.setZona(zona);
                pf.setNumeroFecha(numeroFecha);
                pf.setPartido(partidoGuardado);
                pf.setEstado("PROGRAMADO"); // Estado inicial

                // Estos quedan nulos hasta que se editen en el panel de gestión
                pf.setFecha(null);
                pf.setHora(null);
                pf.setCancha(null);

                programacionRepo.save(pf);
            }

            // Rotación clásica Round Robin
            Equipo ultimo = lista.remove(lista.size() - 1);
            lista.add(1, ultimo);
        }
    }



    @org.springframework.transaction.annotation.Transactional
    public void eliminarProgramacionesDeZona(Long idZona) {
        // Usamos el repositorio para borrar en cascada
        programacionService.eliminarProgramacionesDeZona(idZona);
        partidoRepository.deleteByZonaId(idZona);
    }


    @Transactional
    public void regenerarFixtureZona(Long zonaId) {

        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

        List<Equipo> equipos = zona.getEquiposZona()
                .stream()
                .map(EquipoZona::getEquipo)
                .toList();

        if (equipos.size() < 2) return;

        // 🔹 Última fecha existente
        Integer ultimaFecha =
                partidoRepository.findMaxNumeroFechaByZonaId(zonaId)
                        .orElse(0);

        generarPartidosFaltantes(zona, equipos, ultimaFecha + 1);
    }

    private void generarPartidosFaltantes(
            Zona zona,
            List<Equipo> equipos,
            int fechaInicial
    ) {
        List<Equipo> lista = new ArrayList<>(equipos);

        if (lista.size() % 2 != 0) lista.add(null);

        int n = lista.size();
        int rondas = n - 1;
        int fecha = fechaInicial;

        for (int ronda = 0; ronda < rondas; ronda++) {

            boolean creoAlgoEnFecha = false;

            for (int i = 0; i < n / 2; i++) {
                Equipo a = lista.get(i);
                Equipo b = lista.get(n - 1 - i);

                if (a == null || b == null) continue;

                // 🔹 Si ya existe partido entre A y B → no crear
                boolean existe =
                        partidoRepository.existsEntreEquipos(
                                zona.getId(),
                                a.getId(),
                                b.getId()
                        );

                if (existe) continue;

                Partido p = new Partido();
                p.setZona(zona);
                p.setEquipoLocal(a);
                p.setEquipoVisitante(b);
                p.setNumeroFecha(fecha);
                p.setEstado("PENDIENTE");

                partidoRepository.save(p);
                creoAlgoEnFecha = true;
            }

            if (creoAlgoEnFecha) {
                fecha++;
            }

            // rotación round-robin
            Equipo ultimo = lista.remove(lista.size() - 1);
            lista.add(1, ultimo);
        }
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "programacion", allEntries = true),
            @CacheEvict(value = "torneos", allEntries = true),
            @CacheEvict(value = "tablaPosiciones", allEntries = true) // Si tenés cacheada la tabla
    })
    public void cerrarPartidoDirecto(
            Long partidoId,
            Integer golesLocal,
            Integer golesVisitante
    ) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        if ("FINALIZADO".equals(partido.getEstado())) {
            throw new RuntimeException("El partido ya está cerrado");
        }

        // OBTENER LOS PUNTOS CONFIGURADOS EN EL TORNEO
        // Navegamos: Partido -> Zona -> Torneo
        int ptsGanador = partido.getZona().getTorneo().getPuntosGanador();
        int ptsEmpate = partido.getZona().getTorneo().getPuntosEmpate();

        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);
        partido.setEstado("FINALIZADO");

        if (golesLocal > golesVisitante) {
            partido.setGanador(partido.getEquipoLocal());
        } else if (golesVisitante > golesLocal) {
            partido.setGanador(partido.getEquipoVisitante());
        } else {
            partido.setGanador(null);
        }

        ProgramacionFecha programacionFecha = programacionRepo
                .findByPartidoId(partido.getId())
                .orElseThrow(()-> new RuntimeException("Programacion no encontrada"));

        programacionFecha.setEstado("FINALIZADO");

        EquipoZona ezLocal = equipoZonaRepository
                .findByZonaIdAndEquipoId(partido.getZona().getId(), partido.getEquipoLocal().getId())
                .orElseThrow(() -> new RuntimeException("Equipo local no pertenece a la zona"));

        EquipoZona ezVisitante = equipoZonaRepository
                .findByZonaIdAndEquipoId(partido.getZona().getId(), partido.getEquipoVisitante().getId())
                .orElseThrow(() -> new RuntimeException("Equipo visitante no pertenece a la zona"));

        ezLocal.setPartidosJugados(ezLocal.getPartidosJugados() + 1);
        ezVisitante.setPartidosJugados(ezVisitante.getPartidosJugados() + 1);

        ezLocal.setGolesAFavor(ezLocal.getGolesAFavor() + golesLocal);
        ezLocal.setGolesEnContra(ezLocal.getGolesEnContra() + golesVisitante);

        ezVisitante.setGolesAFavor(ezVisitante.getGolesAFavor() + golesVisitante);
        ezVisitante.setGolesEnContra(ezVisitante.getGolesEnContra() + golesLocal);

        // LÓGICA DE PUNTOS DINÁMICA
        if (golesLocal > golesVisitante) {
            ezLocal.setGanados(ezLocal.getGanados() + 1);
            ezLocal.setPuntos(ezLocal.getPuntos() + ptsGanador); // Usamos ptsGanador del torneo

            ezVisitante.setPerdidos(ezVisitante.getPerdidos() + 1);

        } else if (golesVisitante > golesLocal) {
            ezVisitante.setGanados(ezVisitante.getGanados() + 1);
            ezVisitante.setPuntos(ezVisitante.getPuntos() + ptsGanador); // Usamos ptsGanador del torneo

            ezLocal.setPerdidos(ezLocal.getPerdidos() + 1);

        } else {
            // EMPATE
            ezLocal.setEmpatados(ezLocal.getEmpatados() + 1);
            ezVisitante.setEmpatados(ezVisitante.getEmpatados() + 1);

            ezLocal.setPuntos(ezLocal.getPuntos() + ptsEmpate); // Usamos ptsEmpate del torneo
            ezVisitante.setPuntos(ezVisitante.getPuntos() + ptsEmpate); // Usamos ptsEmpate del torneo
        }

        equipoZonaRepository.save(ezLocal);
        equipoZonaRepository.save(ezVisitante);
        partidoRepository.save(partido);
    }


    @Transactional
    public void solicitarCierre(
            Long partidoId,
            Usuario solicitante,
            Integer golesLocal,
            Integer golesVisitante
    ) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow();

        if (solicitudRepo.existsByPartidoAndEstado(
                partido, "PENDIENTE")) {
            throw new RuntimeException("Ya existe una solicitud pendiente");
        }

        SolicitudCierrePartido solicitud =
                new SolicitudCierrePartido();

        solicitud.setPartido(partido);
        solicitud.setSolicitante(solicitante);
        solicitud.setGolesLocal(golesLocal);
        solicitud.setGolesVisitante(golesVisitante);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());

        solicitudRepo.save(solicitud);
    }

    @Transactional
    public void aprobarSolicitud(Long solicitudId) {
        SolicitudCierrePartido solicitud =
                solicitudRepo.findById(solicitudId).orElseThrow();

        cerrarPartidoDirecto(
                solicitud.getPartido().getId(),
                solicitud.getGolesLocal(),
                solicitud.getGolesVisitante()
        );

        solicitud.setEstado("APROBADA");
        solicitudRepo.save(solicitud);
    }

    @Transactional
    public void rechazarSolicitud(Long solicitudId) {
        SolicitudCierrePartido solicitud =
                solicitudRepo.findById(solicitudId).orElseThrow();

        solicitud.setEstado("RECHAZADA");
        solicitudRepo.save(solicitud);
    }

    public List<Integer> obtenerFechasConPartidos(Long zonaId) {
        // Ahora solo devolverá [1, 2] si solo hay partidos en esas fechas
        return partidoRepository.findDistinctNumeroFechaByZonaId(zonaId);
    }


    @Transactional
    @Caching(evict = {
            // Limpia la programación de la zona/fecha específica
            @CacheEvict(value = "programacion", allEntries = true),
            // Limpia el fixture visual para que se vea la nueva cancha/hora
            @CacheEvict(value = "torneoDetalle", allEntries = true)
    })
    public void actualizarDetallesProgramacion(
            Long partidoId,
            String fechaStr, // Recibimos como String para parsear
            String horaStr,  // Recibimos como String para parsear
            String canchaNombre,
            String arbitro
    ) {
        // 1. Buscamos el registro en la tabla de programación
        ProgramacionFecha pf = programacionRepo.findByPartidoId(partidoId)
                .orElseThrow(() -> new RuntimeException("Este partido aún no ha sido programado en una fecha"));

        // Obtenemos la entidad Partido vinculada
        Partido partido = pf.getPartido();

        // 2. Procesar FECHA (LocalDate)
        if (fechaStr != null && !fechaStr.isEmpty()) {
            java.time.LocalDate fecha = java.time.LocalDate.parse(fechaStr);
            pf.setFecha(fecha);       // Guarda en programacion_fecha
            partido.setFecha(fecha);  // Guarda en partido
        }

        // 3. Procesar HORA (LocalTime)
        if (horaStr != null && !horaStr.isEmpty()) {
            java.time.LocalTime hora = java.time.LocalTime.parse(horaStr);
            pf.setHora(hora);         // Guarda en programacion_fecha
            partido.setHora(hora);    // Guarda en partido
        }

        // 4. Actualizar Cancha
        if (canchaNombre != null && !canchaNombre.isEmpty()) {
            Cancha cancha = canchaRepository.findByNombre(canchaNombre)
                    .orElseGet(() -> {
                        Cancha nueva = new Cancha();
                        nueva.setNombre(canchaNombre);
                        nueva.setEstado(true);
                        return canchaRepository.save(nueva);
                    });
            pf.setCancha(cancha);
            partido.setCancha(cancha);
        }

        // 5. Actualizar Árbitro
        if (arbitro != null && !arbitro.isBlank()) {
            Usuario user = usuarioRepository.findByNombre(arbitro)
                    .orElseThrow(() -> new RuntimeException("El árbitro seleccionado no existe"));

            // Asumiendo que Arbitro extiende de Usuario o usa su ID
            Arbitro realArbitro = new Arbitro();
            realArbitro.setId(user.getId());
            partido.setArbitro(realArbitro);
        }

        // 6. Persistencia final en ambas tablas
        programacionRepo.save(pf);
        partidoRepository.save(partido);
    }


    @Transactional
    public void eliminarYRestaurarPartidosPorEquipo(Long zonaId, Long equipoId) {
        // 1. Buscar todos los partidos del equipo en esa zona (Local o Visitante)
        List<Partido> partidos = partidoRepository.findAllByZonaIdAndEquipoId(zonaId, equipoId);

        for (Partido partido : partidos) {
            // A. Borrar programación siempre (si existe) para evitar error de FK
            programacionService.eliminarProgramacionPorPartido(partido.getId());

            if ("FINALIZADO".equals(partido.getEstado())) {
                // B. Si el partido se jugó, actualizamos al RIVAL para restarle los puntos/goles
                actualizarEstadisticasRivalPorEliminacion(partido, equipoId);
            }

            // C. Borrar el partido físicamente
            partidoRepository.delete(partido);
        }
    }

    private void actualizarEstadisticasRivalPorEliminacion(Partido partido, Long equipoEliminadoId) {
        boolean fueLocalElEliminado = partido.getEquipoLocal().getId().equals(equipoEliminadoId);
        Long rivalId = fueLocalElEliminado ? partido.getEquipoVisitante().getId() : partido.getEquipoLocal().getId();

        // Buscamos la fila del rival en la tabla de posiciones
        EquipoZona rivalZona = equipoZonaRepository.findByZonaIdAndEquipoId(partido.getZona().getId(), rivalId)
                .orElse(null);

        if (rivalZona != null) {
            int golesRival = fueLocalElEliminado ? partido.getGolesVisitante() : partido.getGolesLocal();
            int golesEliminado = fueLocalElEliminado ? partido.getGolesLocal() : partido.getGolesVisitante();

            // REVERSA DE ESTADÍSTICAS
            rivalZona.setPartidosJugados(rivalZona.getPartidosJugados() - 1);
            rivalZona.setGolesAFavor(rivalZona.getGolesAFavor() - golesRival);
            rivalZona.setGolesEnContra(rivalZona.getGolesEnContra() - golesEliminado);

            // Reversa de Puntos y Resultados
            if (golesRival > golesEliminado) { // El rival había ganado
                rivalZona.setGanados(rivalZona.getGanados() - 1);
                rivalZona.setPuntos(rivalZona.getPuntos() - partido.getZona().getTorneo().getPuntosGanador());
            } else if (golesRival == golesEliminado) { // Habían empatado
                rivalZona.setEmpatados(rivalZona.getEmpatados() - 1);
                rivalZona.setPuntos(rivalZona.getPuntos() - 1);
            } else { // El rival había perdido
                rivalZona.setPerdidos(rivalZona.getPerdidos() - 1);
            }

            equipoZonaRepository.save(rivalZona);
        }
    }

    public boolean existsByEtapaId(Long etapaId) {
        return partidoRepository.existsByEtapaId(etapaId);
    }
}
