package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.ReclamacionesRequest;
import com.ecommerce.api_geek_store.api.dto.ReclamacionesResponse;
import com.ecommerce.api_geek_store.domain.model.Reclamacion;
import org.springframework.stereotype.Component;

@Component
public class ReclamacionesMapper {

    public Reclamacion toEntity(ReclamacionesRequest request) {
        if (request == null) return null;

        Reclamacion entidad = new Reclamacion();
        entidad.setNombreCompleto(request.nombreCompleto());
        entidad.setDni(request.dni());
        entidad.setTelefono(request.telefono());
        entidad.setEmail(request.email());
        entidad.setDireccion(request.direccion());
        entidad.setTipoBien(request.tipoBien());
        entidad.setMontoReclamado(request.montoReclamado());
        entidad.setDescripcionBien(request.descripcionBien());
        entidad.setTipoReclamo(request.tipoReclamo());
        entidad.setDetalleProblema(request.detalleProblema());
        entidad.setPedidoConsumidor(request.pedidoConsumidor());
        entidad.setResuelto(false);
        return entidad;
    }

    public ReclamacionesResponse toResponse(Reclamacion entidad) {
        if (entidad == null) return null;

        return new ReclamacionesResponse(
                entidad.getId(),
                entidad.getCodigoReclamacion(),
                entidad.getFechaCreacion(),
                entidad.getNombreCompleto(),
                entidad.getEmail(),
                entidad.getTipoReclamo(),
                entidad.getDetalleProblema(),
                entidad.getPedidoConsumidor(),
                entidad.getDni(),
                entidad.getTelefono(),
                entidad.getDireccion(),
                entidad.getResuelto()
        );
    }
}