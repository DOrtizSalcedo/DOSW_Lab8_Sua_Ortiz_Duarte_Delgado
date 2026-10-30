package edu.eci.dosw.oficioya.repository;

import edu.eci.dosw.oficioya.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByContratanteId(Long contratanteId);
    List<Solicitud> findByTrabajadorId(Long trabajadorId);

    // La consulta incluye al participante: una solicitud ajena y una q no existe dan el mismo 404
    Optional<Solicitud> findByIdAndContratanteId(Long id, Long contratanteId);
    Optional<Solicitud> findByIdAndTrabajadorId(Long id, Long trabajadorId);

    @Query("SELECT s FROM Solicitud s WHERE s.id = :id " +
            "AND (s.contratante.id = :usuarioId OR s.trabajador.id = :usuarioId)")
    Optional<Solicitud> findByIdAndParticipante(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}
