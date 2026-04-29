package com.apteka.portal.services;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apteka.portal.repository.ClientRepository;

public class ClientServiceTest {
    @Mock
    private ClientRepository clientInterface;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }
}
