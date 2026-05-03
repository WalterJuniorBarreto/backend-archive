package com.ecommerce.api_geek_store.domain.model;

import com.thoughtworks.qdox.model.expression.Add;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;



@Entity
@Table(name = "addresses",
        //Hacemos la consulta mas rapida para el campo user y solo para las direcciones activas
        indexes = {
                @Index(name = "idx_address_user", columnList = "user_id, deleted")
        }
)



//No hace falta escribir getters ni setters gracias a lombok
@Getter
@Setter
//Constructor vacio y lleno
@NoArgsConstructor
@AllArgsConstructor
//Facilita la creacion de objetos
@Builder
//Esta entidad tiene un listener es un AuditinEntityListener permite llenar automaticamente campos sin codigo
@EntityListeners(AuditingEntityListener.class)
//Cuando el usuario borra internamente no se borra si no lo mantenemos en estado eliminado para mantener el historial por eso aca hacemos un UPDATE de deleted
@SQLDelete(sql = "UPDATE addresses SET deleted = true WHERE id = ?")
//Todas las consultas que hagamos esto tendra al final como tipo filtro
@SQLRestriction("deleted = false")



public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(length = 200)
    private String referencia;

    @Column(nullable = false, length = 20)
    private String codigoPostal;

    //Muchos a uno
    //Un usuario puede tener muchas direcciones
    //Fetch Lazy no cargues el usuario cuando llame a la direccion
    @ManyToOne(fetch = FetchType.LAZY)
    //Aca esta la columna que une user y address
    //Name muy importante
    //No se permite una direccion sin usuario
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    //Cuando una direccion se crae automaticamente este campo se crea gracais a la auditoria
    //Con @CreatedDate añadimos
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;




    //Con @LastModifiedDate cuando se actualize la direccion esto automaticamente
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    //Importante @Builder.Default asi respeta la inicializacion de deleted si no NO
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;




    //Importante cuando java trae cierta direccion lo guarda en la RAM con diferente id por eso el metodo equals
    @Override
    public boolean equals(Object o) {
        //Si es el mismo objeto comparado consigo mismo ok true
        if (this == o) return true;
        //Si este objeto no es una instancia de Addres retorname false
        if (!(o instanceof Address)) return false;
        //Sabemos que es una direccion el objeto asi que lo ponemos la etiqueta Address para acceder a sus metodos
        Address address = (Address) o;
        //Devuelve un id que no es null evitando comparar nuevas entidades
        //Compara los id de ambos evitando nullpointer
        return id != null && id.equals(address.getId());
    }



    //Esta funcion devuelve un entero fijo y constante para todo los set y map usan ese numero para saber donde se guardo el objeto...
    //
    @Override
    public int hashCode() {
        //Esto le dice que todos tienen el mismo codigo gracias a esto el entero nunca cambia asi set y map no pierden objetos
        return getClass().hashCode();
    }





}

