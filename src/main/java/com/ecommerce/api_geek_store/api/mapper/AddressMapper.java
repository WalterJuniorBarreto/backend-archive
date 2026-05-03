package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.AddressRequest;
import com.ecommerce.api_geek_store.api.dto.AddressResponse;
import com.ecommerce.api_geek_store.api.dto.AddressUpdateRequest;
import com.ecommerce.api_geek_store.domain.model.Address;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toEntity(AddressRequest request);
    AddressResponse toResponse(Address address);

    //Si un campo viene null ignoramelo
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    //estos datos no me los toques
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    //no me creas una nueva entidad si no la misma la actualizas
    void updateAddressFromDto(AddressUpdateRequest request, @MappingTarget Address entity);
}



