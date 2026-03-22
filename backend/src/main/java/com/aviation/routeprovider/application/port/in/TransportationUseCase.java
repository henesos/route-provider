package com.aviation.routeprovider.application.port.in;

import com.aviation.routeprovider.domain.model.entity.Transportation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransportationUseCase {

    Transportation createTransportation(CreateTransportationCommand command);

    Transportation updateTransportation(Long id, UpdateTransportationCommand command);

    void deleteTransportation(Long id);

    Transportation getTransportation(Long id);

    Page<Transportation> getAllTransportations(Pageable pageable);
}
