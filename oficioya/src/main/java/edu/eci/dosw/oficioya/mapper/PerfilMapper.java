package edu.eci.dosw.oficioya.mapper;

import edu.eci.dosw.oficioya.dto.PerfilPropio;
import edu.eci.dosw.oficioya.entity.Usuario;
import org.springframework.stereotype.Component;
import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.dto.PerfilAdmin;

@Component
public class PerfilMapper {

    public PerfilPropio toPropio(Usuario u) {
        return new PerfilPropio(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getTelefono(),
                u.getRol(),
                u.getOficio_principal(),
                u.getZona());
    }

    public PerfilPublico toPublico(Usuario u) {
        return new PerfilPublico(
                u.getId(),
                u.getNombre(),
                u.getOficio_principal(),
                u.getZona());
    }

    public PerfilAdmin toAdmin(Usuario u) {
        return new PerfilAdmin(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getTelefono(),
                u.getRol(),
                u.getOficio_principal(),
                u.getZona());
    }
}