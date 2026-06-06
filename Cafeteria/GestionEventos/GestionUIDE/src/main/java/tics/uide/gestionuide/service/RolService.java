package tics.uide.gestionuide.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.enums.Rol;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.Roles;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.repository.RolRepository;

@Service
@Transactional
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public Roles agregarRol(Usuario usuario, Rol rol) {
        if (rolRepository.existsByUsuarioAndRol(usuario, rol)) {
            throw new BadRequestException("El usuario ya tiene el rol: " + rol.name());
        }

        Roles roles = Roles.builder()
                .usuario(usuario)
                .rol(rol)
                .build();

        return rolRepository.save(roles);
    }

    public List<Roles> obtenerRolesDeUsuario(Usuario usuario) {
        return rolRepository.findByUsuario(usuario);
    }

    public List<Roles> obtenerRolesDeUsuario(Long usuarioId) {
        return rolRepository.findByUsuario_Id(usuarioId);
    }

    public boolean usuarioTieneRol(Usuario usuario, Rol rol) {
        return rolRepository.existsByUsuarioAndRol(usuario, rol);
    }

    public boolean usuarioTieneRol(Long usuarioId, Rol rol) {
        return rolRepository.existsByUsuario_IdAndRol(usuarioId, rol);
    }

    public void eliminarRol(Usuario usuario, Rol rol) {
        List<Roles> roles = rolRepository.findByUsuarioAndRol(usuario, rol);
        if (!roles.isEmpty()) {
            rolRepository.deleteAll(roles);
        }
    }

    public void eliminarTodosLosRoles(Usuario usuario) {
        List<Roles> roles = rolRepository.findByUsuario(usuario);
        rolRepository.deleteAll(roles);
    }

    public void reemplazarRoles(Usuario usuario, List<Rol> nuevosRoles) {
        eliminarTodosLosRoles(usuario);
        for (Rol rol : nuevosRoles) {
            agregarRol(usuario, rol);
        }
    }
}
