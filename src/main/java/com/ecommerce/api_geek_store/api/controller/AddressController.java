package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.AddressRequest;
import com.ecommerce.api_geek_store.api.dto.AddressResponse;
import com.ecommerce.api_geek_store.api.dto.AddressUpdateRequest;
import com.ecommerce.api_geek_store.api.dto.UserResponse;
import com.ecommerce.api_geek_store.domain.model.Address;
import com.ecommerce.api_geek_store.domain.model.User;
import com.ecommerce.api_geek_store.service.AddressService;
import com.thoughtworks.qdox.model.expression.Add;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/addresses")
//Genera un constructor internamente de los que tienen final
@RequiredArgsConstructor
//Genera un logger automaticamente
@Slf4j
//Solo ejecuta este controller si el usuario esta autenticado
@PreAuthorize("isAuthenticated()")



public class AddressController {


    private final AddressService addressService;

    //Metodo get
    //Sin url hereda de RequestMapping
    @GetMapping
    //Response una lista de direcciones
    //Page paginas de direcciones
    //AuthenticationPrincipal extrae el usuario de SegurityContext
    //lo inyecta en UserDetails interfaz de Java, Spring segurity solo entiende eso no entidades.
    //Pageable objeto que contiene toda la info de Page
    //@PageableDefault establece valores por defecto si el frontend no envia parametros de paginacion
    //10 elementos por pagina ordenados por id
    public ResponseEntity<Page<AddressResponse>> getMyAddresses(@AuthenticationPrincipal UserDetails userDetails, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        //pageable.getPageNumber devuelve el numero de paginas que pidio el usuario
        log.info("Usuario {} listando direcciones (Página: {})", userDetails.getUsername(), pageable.getPageNumber());
        //Llamamos al services pasandole el username
        //Response un 200 ok
        return ResponseEntity.ok(addressService.getMyAddresses(userDetails.getUsername(), pageable));
    }




    //Peticiones POST como crear direccion
    @PostMapping
    //@RequestBody el cuerpo JSON de la request conviertelo en un AddressRequest
    //@Valid activa validaciones de lo que tenga el DTO
    public ResponseEntity<AddressResponse> createAddress(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid AddressRequest request) {
        //llamamos al metodo crear del service pasandole el user y el request
        AddressResponse newAddress = addressService.createAddress(userDetails.getUsername(), request);
        //Creamos la location importante para REST cuando creamos algo
        //ServletUriComponentsBuilder construye la URL del recurso creado
        //fromCurrentRequest tomamos la URL base
        //path "/{id}" le agregamos el id
        //buildAndExpand(newAdress.id()) le agregamos el id real
        //toUri() convertimos a URI ya que headers trabaja con URI
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newAddress.id())
                .toUri();

        //Registra un evento log.info "{}"
        log.info("Dirección creada exitosamente. ID: {}", newAddress.id());
        //Devuelve 201, agrega el location con la URL del recurso creado y manda el recurso en el body
        return ResponseEntity.created(location).body(newAddress);


    }




    //Put es para actualizar con su id
    @PutMapping("/{id}")
    //@PathVariable agarramos el id de la url
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody @Valid AddressUpdateRequest request) {
        log.info("Usuario {} actualizando dirección ID: {}", userDetails.getUsername(), id);
        return ResponseEntity.ok(addressService.updateAddress(id, userDetails.getUsername(), request));
    }



    @DeleteMapping("/{id}")
    //Void solo devuelve codigo de estado sin cuerpo
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        log.info("Usuario {} eliminando dirección ID: {}", userDetails.getUsername(), id);
        addressService.deleteAddress(userDetails.getUsername(), id);
        //NoContent un 204 sin body exitoso
        //Build devuelve un RE
        return ResponseEntity.noContent().build();
    }






}

