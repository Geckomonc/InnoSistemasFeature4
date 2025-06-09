package com.InnoSistemas.Feature_4.service;

import com.InnoSistemas.Feature_4.dto.EventoHistorialResponseDTO;
import com.InnoSistemas.Feature_4.dto.EventoRequestDTO;
import com.InnoSistemas.Feature_4.entity.*;
import com.InnoSistemas.Feature_4.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService {

    @Autowired private EventoRepository eventoRepository;
    @Autowired private PrioridadRepository prioridadRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private ProyectoRepository proyectoRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private TipoEventoRepository tipoEventoRepository;
    @Autowired private EmailService emailService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EventoUsuarioRepository eventoUsuarioRepository; //  agregado
    @Autowired private UsuarioProyectoRepository usuarioProyectoRepository;


    public Evento registrarEvento(EventoRequestDTO dto) {
        Evento evento = new Evento();
        evento.setNombre(dto.getNombre());
        evento.setFecha(dto.getFecha());
        evento.setHora(dto.getHora());
        evento.setDescripcion(dto.getDescripcion());
        evento.setRealizado(dto.getRealizado());
        evento.setPrioridad(prioridadRepository.findById(dto.getIdPrioridad()).orElseThrow());
        evento.setEquipo(equipoRepository.findById(dto.getIdEquipo()).orElseThrow());
        evento.setProyecto(proyectoRepository.findById(dto.getIdProyecto()).orElseThrow());
        evento.setCurso(cursoRepository.findById(dto.getIdCurso()).orElseThrow());
        evento.setTipoEvento(tipoEventoRepository.findById(dto.getIdTipoEvento()).orElseThrow());

        Evento nuevoEvento = eventoRepository.save(evento);

        // Guardar en tbleventoxusuario y enviar correo
        equipoRepository.findById(dto.getIdEquipo()).ifPresent(equipo -> {
            equipo.getUsuarios().forEach(usuario -> {
                // Guardar relación evento-usuario
                EventoUsuario eventoUsuario = new EventoUsuario();
                eventoUsuario.setEvento(nuevoEvento);
                eventoUsuario.setUsuario(usuario);
                eventoUsuarioRepository.save(eventoUsuario);

                // Enviar correo
                emailService.enviarCorreo(
                        usuario.getCorreo(),
                        "Nuevo Evento: " + evento.getNombre(),
                        "Hola " + usuario.getNombre() + ",\n\nTienes un nuevo evento:\n" +
                                evento.getNombre() + "\nDescripción: " + evento.getDescripcion() +
                                "\nFecha: " + evento.getFecha() + " " + evento.getHora() +
                                "\n\n¡No lo olvides!"
                );
            });
        });

        return nuevoEvento;
    }
    public List<EventoHistorialResponseDTO> obtenerEventosPorUsuario(Integer id_usuario) {
        List<EventoUsuario> relaciones = eventoUsuarioRepository.findByUsuarioId(id_usuario);
        return relaciones.stream().map(rel -> {
            Evento evento = rel.getEvento();
            EventoHistorialResponseDTO dto = new EventoHistorialResponseDTO();
            dto.setNombre(evento.getNombre());
            dto.setDescripcion(evento.getDescripcion());
            dto.setFecha(evento.getFecha());
            dto.setHora(evento.getHora());
            dto.setEquipo(evento.getEquipo().getEquipo()); // Asegúrate de tener el campo
            dto.setProyecto(evento.getProyecto().getProyecto());
            dto.setCurso(evento.getCurso().getCurso());
            dto.setTipo(evento.getTipoEvento().getTipo());
            return dto;
        }).collect(Collectors.toList());
    }

}
