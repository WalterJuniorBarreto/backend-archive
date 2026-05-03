package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.ReclamacionesRequest;
import com.ecommerce.api_geek_store.api.dto.ReclamacionesResponse;

import java.util.List;

public interface ReclamacionService {


    ReclamacionesResponse registrarReclamo(ReclamacionesRequest request);
    List<ReclamacionesResponse> listarTodos();
    ReclamacionesResponse cambiarEstado(Long id, Boolean resuelto);
}