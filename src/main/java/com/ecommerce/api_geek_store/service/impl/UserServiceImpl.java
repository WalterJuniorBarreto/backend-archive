package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.*;
import com.ecommerce.api_geek_store.domain.model.AuditLog;
import com.ecommerce.api_geek_store.domain.model.AuthProvider;
import com.ecommerce.api_geek_store.domain.model.Role;
import com.ecommerce.api_geek_store.domain.model.User;
import com.ecommerce.api_geek_store.domain.repository.AuditLogRepository;
import com.ecommerce.api_geek_store.domain.repository.UserRepository;
import com.ecommerce.api_geek_store.exception.InvalidPasswordException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ecommerce.api_geek_store.api.mapper.UserMapper;

import javax.security.auth.callback.TextInputCallback;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditLogRepository auditLogRepository;



    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));


        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("La contraseña antigua es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

    }


    @Override
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return userMapper.toResponse(user);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String keyword, Pageable pageable, UserDetails authUser){
        //getAuthorities nos da una lista de roles o permisos del usuario
        //Con anyMatch vemos si existe algun elemento que cumpla la condicion
        //Con getAuthotity nos da los roles comparamos con equals con ROLE ADMIN
        boolean isAdmin = authUser.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));


        String safeKeyword = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();


        if (isAdmin) {
            if (safeKeyword.isEmpty()) {
                return userRepository.findAll(pageable).map(userMapper::toResponse);
            }
            return userRepository.searchUsers(safeKeyword, pageable).map(userMapper::toResponse);
        } else {
            if (safeKeyword.isEmpty()) {
                return userRepository.findByRol("ROLE_USER", pageable).map(userMapper::toResponse);
            }
            return userRepository.searchOnlyCustomers(safeKeyword, pageable).map(userMapper::toResponse);
        }
    }




    @Override
    @Transactional
    public UserResponse findById(Long id, UserDetails authUser, String motivo) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        String emailLogueado = authUser.getUsername();
        User empleadoLogueado = userRepository.findByEmail(emailLogueado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado logueado no encontrado en la base de datos"));
        boolean isAdmin = (empleadoLogueado.getRol() == Role.ROLE_ADMIN);

        if (!isAdmin && targetUser.getRol() != Role.ROLE_USER) {
            throw new AccessDeniedException("Acceso denegado: Como Soporte, solo puedes ver los perfiles de los Clientes.");
        }
        registrarAuditoria(empleadoLogueado.getEmail(), empleadoLogueado.getRol(), "VER_DETALLES", targetUser.getId(), targetUser.getRol(), motivo);

        return userMapper.toResponse(targetUser);
    }





    @Override
    @Transactional
    public UserResponse create(AdminUserRequest request, UserDetails authUser, String motivo) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado en el sistema");
        }
        User empleadoLogueado = (User) authUser;
        boolean isAdmin = (empleadoLogueado.getRol() == Role.ROLE_ADMIN);
        Role rolSolicitado = parseRole(request.rol());

        if (!isAdmin && rolSolicitado != Role.ROLE_USER) {
            throw new AccessDeniedException("Fraude detectado: Como personal de Soporte, solo tienes permisos para crear cuentas de clientes (USER).");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRol(rolSolicitado);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        registrarAuditoria(
                empleadoLogueado.getEmail(),
                empleadoLogueado.getRol(),
                "CREAR_USUARIO",
                savedUser.getId(),
                savedUser.getRol(),
                motivo
        );

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, AdminUserUpdateRequest request, UserDetails authUser, String motivo) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        User empleadoLogueado = (User) authUser;
        boolean isAdmin = (empleadoLogueado.getRol() == Role.ROLE_ADMIN);
        Role rolSolicitado = (request.rol() != null) ? parseRole(request.rol()) : null;
        if (!isAdmin) {
            if (targetUser.getRol() != Role.ROLE_USER) {
                throw new AccessDeniedException("Acceso denegado: No puedes editar el perfil de un Administrador o de un compañero de Soporte.");
            }
            if (rolSolicitado != null && rolSolicitado != Role.ROLE_USER) {
                throw new AccessDeniedException("Acceso denegado: No tienes permisos para asignar roles superiores a USER.");
            }
        }


        if (isAdmin && targetUser.getId().equals(empleadoLogueado.getId())) {
            if (rolSolicitado != null && rolSolicitado != Role.ROLE_ADMIN) {
                throw new AccessDeniedException("No puedes quitarte tu propio rol de Administrador. Otro Admin debe hacerlo.");
            }
        }

        if (request.email() != null && !request.email().equalsIgnoreCase(targetUser.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("El email ya está en uso por otro usuario.");
            }
            targetUser.setEmail(request.email());
        }
        if (rolSolicitado != null) {
            targetUser.setRol(isAdmin ? rolSolicitado : Role.ROLE_USER);
        }
        userMapper.updateEntityFromDto(request, targetUser);
        User savedUser = userRepository.save(targetUser);

        registrarAuditoria(
                empleadoLogueado.getEmail(),
                empleadoLogueado.getRol(),
                "ACTUALIZAR_PERFIL",
                savedUser.getId(),
                savedUser.getRol(),
                motivo
        );

        return userMapper.toResponse(savedUser);
    }


    @Override
    @Transactional
    public void toggleStatus(Long id, UserDetails authUser, String motivo) {

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        User empleadoLogueado = (User) authUser;
        boolean isAdmin = (empleadoLogueado.getRol() == Role.ROLE_ADMIN);

        if (!isAdmin && targetUser.getRol() != Role.ROLE_USER) {
            throw new AccessDeniedException("Operación Denegada: Soporte solo puede modificar el acceso de los Clientes.");
        }


        if (targetUser.getId().equals(empleadoLogueado.getId())) {
            throw new AccessDeniedException("Operación Crítica Denegada: No puedes banear o desactivar tu propia cuenta.");
        }


        targetUser.setEnabled(!targetUser.isEnabled());

        userRepository.save(targetUser);
        String accion = targetUser.isEnabled() ? "ACTIVAR_USUARIO" : "BANEAR_USUARIO";


        registrarAuditoria(
                empleadoLogueado.getEmail(),
                empleadoLogueado.getRol(),
                accion,
                targetUser.getId(),
                targetUser.getRol(),
                motivo
        );

        log.info("Auditoría Sistema: Usuario ID [{}] ha sido [{}] por el empleado [{}]",
                id, targetUser.isEnabled() ? "ACTIVADO" : "BANEADO", empleadoLogueado.getEmail());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if(request.nombre() != null) user.setNombre(request.nombre());
        if(request.apellido() != null) user.setApellido(request.apellido());
        if(request.dni() != null) user.setDni(request.dni());
        if(request.telefono() != null) user.setTelefono(request.telefono());
        if(request.genero() != null) user.setGenero(request.genero());
        if(request.fechaNacimiento() != null) user.setFechaNacimiento(request.fechaNacimiento());


        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }


    private void registrarAuditoria(String empleadoEmail, Role empleadoRol, String accion, Long usuarioAfectadoId, Role usuarioAfectadoRol, String motivo) {
        boolean isAdmin = (empleadoRol == Role.ROLE_ADMIN);
        if (!isAdmin && (motivo == null || motivo.trim().isEmpty())) {
            throw new IllegalArgumentException("Seguridad: El motivo es obligatorio para el personal de Soporte.");
        }
        String motivoFinal = (motivo != null && !motivo.trim().isEmpty()) ? motivo.trim() : "Acción directa de Administrador (Sin motivo especificado)";
        AuditLog auditLog = AuditLog.builder()
                .empleadoEmail(empleadoEmail)
                .empleadoRol(empleadoRol)
                .accion(accion)
                .usuarioAfectadoId(usuarioAfectadoId)
                .usuarioAfectadoRol(usuarioAfectadoRol)
                .motivo(motivoFinal)
                .build();
        auditLogRepository.save(auditLog);
    }


    private Role parseRole(String roleString) {
        return switch (roleString.toUpperCase()) {
            case "ADMIN" -> Role.ROLE_ADMIN;
            case "SOPORTE" -> Role.ROLE_SOPORTE;
            default -> Role.ROLE_USER;
        };
    }




}