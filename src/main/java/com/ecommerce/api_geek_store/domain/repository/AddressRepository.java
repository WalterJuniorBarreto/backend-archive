package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.Address;
import com.thoughtworks.qdox.model.expression.Add;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    //Usamos page y no List ya que con page no saturamos la red si tenemos 1000 direcciones traerlas
    //todo de golpe seria muy fuerte con page controlamos de cuanto en cuanto traemos
    Page<Address> findByUserId(Long userId, Pageable pageable);



    //Cuenta cuantas direcciones tiene ese usuario
    long countByUserId(Long userId);


}





