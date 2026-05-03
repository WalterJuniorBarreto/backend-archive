package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.ReclamacionesRequest;
import com.ecommerce.api_geek_store.api.dto.ReclamacionesResponse;
import com.ecommerce.api_geek_store.api.mapper.ReclamacionesMapper;
import com.ecommerce.api_geek_store.domain.model.Reclamacion;
import com.ecommerce.api_geek_store.domain.repository.ReclamacionRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.ReclamacionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReclamacionServiceImpl implements ReclamacionService {

    private final ReclamacionRepository reclamacionRepository;
    private final ReclamacionesMapper reclamacionMapper;

    public ReclamacionServiceImpl(ReclamacionRepository reclamacionRepository,
                                  ReclamacionesMapper reclamacionMapper) {
        this.reclamacionRepository = reclamacionRepository;
        this.reclamacionMapper = reclamacionMapper;
    }

    @Override
    @Transactional
    public ReclamacionesResponse registrarReclamo(ReclamacionesRequest request) {
        Reclamacion entidad = reclamacionMapper.toEntity(request);


        entidad.setCodigoReclamacion("GENERANDO...");
        Reclamacion guardada = reclamacionRepository.save(entidad);


        String anio = String.valueOf(Year.now().getValue());
        String codigoGenerado = String.format("REC-%s-%04d", anio, guardada.getId());

        guardada.setCodigoReclamacion(codigoGenerado);


        return reclamacionMapper.toResponse(reclamacionRepository.save(guardada));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamacionesResponse> listarTodos() {
        return reclamacionRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(reclamacionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReclamacionesResponse cambiarEstado(Long id, Boolean resuelto) {
        Reclamacion reclamacion = reclamacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo no encontrado con id: " + id));

        reclamacion.setResuelto(resuelto);
        Reclamacion actualizada = reclamacionRepository.save(reclamacion);

        return reclamacionMapper.toResponse(actualizada);
    }
}