package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.AdminUserRequest;
import com.ecommerce.api_geek_store.api.dto.AdminUserUpdateRequest;
import com.ecommerce.api_geek_store.api.dto.RegisterRequest;
import com.ecommerce.api_geek_store.api.dto.UserResponse;
import com.ecommerce.api_geek_store.domain.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequest request);
    User toEntity(AdminUserRequest request);
    UserResponse toResponse(User user);

    //BeanMapping sirve para configurar el MapStruc
    //nullValuePropertyMappingStrategy  es para definir que hacemos cuando el campo viene en null
    //IGNORE si es null mapper no lo copia en la entidad
    //Mapping significa que el mapper no lo actualiza por el ignore true lo podemos hacer en el service
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    //MappingTarget no lo crees actualizalo
    void updateEntityFromDto(AdminUserUpdateRequest request, @MappingTarget User entity);
}


