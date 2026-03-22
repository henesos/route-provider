package com.aviation.routeprovider.application.port.out;

import com.aviation.routeprovider.domain.model.entity.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

}
