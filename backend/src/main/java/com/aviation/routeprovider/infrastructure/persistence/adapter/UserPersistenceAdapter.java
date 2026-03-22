package com.aviation.routeprovider.infrastructure.persistence.adapter;

import com.aviation.routeprovider.application.port.out.UserRepository;
import com.aviation.routeprovider.domain.model.entity.User;
import com.aviation.routeprovider.infrastructure.persistence.entity.UserJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class UserPersistenceAdapter implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    
    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = new UserJpaEntity();
        jpaEntity.setId(user.getId());
        jpaEntity.setUsername(user.getUsername());
        jpaEntity.setPasswordHash(user.getPasswordHash());
        jpaEntity.setRole(user.getRole());
        
        UserJpaEntity saved = jpaRepository.save(jpaEntity);
        return User.reconstruct(
            saved.getId(),
            saved.getUsername(),
            saved.getPasswordHash(),
            saved.getRole()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
            .map(jpaEntity -> User.reconstruct(
                jpaEntity.getId(),
                jpaEntity.getUsername(),
                jpaEntity.getPasswordHash(),
                jpaEntity.getRole()
            ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
            .map(jpaEntity -> User.reconstruct(
                jpaEntity.getId(),
                jpaEntity.getUsername(),
                jpaEntity.getPasswordHash(),
                jpaEntity.getRole()
            ));
    }
}
