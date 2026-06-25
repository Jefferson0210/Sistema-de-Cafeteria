package com.cafeteria.app.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.enums.Rol;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.Roles;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.RolRepository;

@Service
@Transactional
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private AuditService auditService;

    public Roles agregarRol(Usuario usuario, Rol rol) {
        if (rolRepository.existsByUsuarioAndRol(usuario, rol)) {
            throw new BadRequestException("El usuario ya tiene el rol: " + rol.name());
        }

        Roles roles = Roles.builder()
                .usuario(usuario)
                .rol(rol)
                .build();

        Roles guardado = rolRepository.save(roles);
        auditService.registrarSiAutenticado("ROL_AGREGADO", "Usuario", usuario.getId(), "rol=" + rol.name());
        return guardado;
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
            auditService.registrarSiAutenticado("ROL_ELIMINADO", "Usuario", usuario.getId(), "rol=" + rol.name());
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
