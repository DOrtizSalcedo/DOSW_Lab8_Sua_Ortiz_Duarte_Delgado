package edu.eci.dosw.oficioya.mapper;

import edu.eci.dosw.oficioya.dto.UsuarioResponse;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public UsuarioResponse toUsuarioResponse(Usuario usuario){
        if (usuario == null) return null;

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getOficio_principal(),
                usuario.getZona(),
                usuario.getFecha_creacion()
        );
    }
}
