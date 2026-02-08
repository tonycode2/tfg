package com.anthony.tfg.tfg.Modulos.Seguridad.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAll_respondeLista() {
        assertNotNull(userRepository);
        assertNotNull(userRepository.findAll());
    }
}
