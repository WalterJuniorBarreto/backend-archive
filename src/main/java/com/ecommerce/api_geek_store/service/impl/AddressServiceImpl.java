package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.AddressRequest;
import com.ecommerce.api_geek_store.api.dto.AddressResponse;
import com.ecommerce.api_geek_store.api.dto.AddressUpdateRequest;
import com.ecommerce.api_geek_store.api.mapper.AddressMapper;
import com.ecommerce.api_geek_store.domain.model.Address;
import com.ecommerce.api_geek_store.domain.model.User;
import com.ecommerce.api_geek_store.domain.repository.AddressRepository;
import com.ecommerce.api_geek_store.domain.repository.UserRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.AddressService;
import com.thoughtworks.qdox.model.expression.Add;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.util.PublicSuffixList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.stream.Collectors;

//LoggerFactory.getLogger(AddressController.class) este logger pertenece a este controller
//Solo esta clase lo usa private
//Se crea una sola vez static
//Inmutable final
@Slf4j
@RequiredArgsConstructor
@Service
public class AddressServiceImpl implements AddressService {



    private static final int MAX_ADDRESSES_PER_USER = 2;


    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    //Importante le decimos a la BD que ya no se prepare para cambios que solo voy a leer
    @Transactional(readOnly = true)
    //Devuelve paginas del dto
    //El parametro viene del controller el username de userdetails
    public Page<AddressResponse> getMyAddresses(String email, Pageable pageable) {
        //Buscamos el usuario
        User user = userRepository.findByEmail(email)
                //Si no existe lanza esta excepcion personalizada
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        //Obtenemos las paginas y lo guardamos en paginas
        Page<Address> addressPage = addressRepository.findByUserId(user.getId(), pageable);
        //Cada elemento pasamos a map y cada Address se convierte a toResponse
        return addressPage.map(addressMapper::toResponse);

    }



    @Override
    //Garantiza que todas las operaciones de bd se ejecuten
    @Transactional
    public AddressResponse createAddress(String email, AddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        //Cuenta cuantas direcciones tiene ese usuario
        long count = addressRepository.countByUserId(user.getId());

        if (count >= MAX_ADDRESSES_PER_USER) {
            //warn paso algo pero normal el sistema continua
            log.warn("Usuario {} intentó exceder límite...", email);
            //ejecutamos la exception
            throw new IllegalArgumentException("Límite de direcciones alcanzado.");
        }
        //Guardamos el request y lo convertimos a entidad con mapper
        //Agregamos al usuario que pertenece estas direcciones enlazar realaciones
        //Guardamos la direccion


        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        Address savedAddress = addressRepository.save(address);
        //Un log de info indicando que ya se guardo
        log.info("Dirección creada ID: {}", savedAddress.getId());
        return addressMapper.toResponse(savedAddress);
    }






    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, String email, AddressUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        //Obtiene el id del usuario dueño de esta direccion
        //Obtiene el id del usuario del inicio de sesion
        //Con Equals los compara
        //Si no son iguales entra
        if (!address.getUser().getId().equals(user.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Usuario {} intentó modificar dirección ajena ID {}", email, addressId);
            throw new AccessDeniedException("No tienes permiso para editar esta dirección.");
        }

        addressMapper.updateAddressFromDto(request, address);
        Address updatedAddress = addressRepository.save(address);
        log.info("Dirección ID {} actualizada por usuario {}", addressId, email);

        return addressMapper.toResponse(updatedAddress);
    }



    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
        if (!address.getUser().getId().equals(user.getId())) {
            log.warn("ALERTA DE SEGURIDAD: Usuario {} intentó borrar dirección ajena ID {}", email, addressId);
            throw new AccessDeniedException("No tienes permiso para eliminar esta dirección.");
        }
        addressRepository.delete(address);
        log.info("Dirección ID {} eliminada por usuario {}", addressId, email);
    }








}
