package com.apteka.portal.services;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apteka.portal.repository.ClientInterface;

public class ClientServiceTest {
    @Mock
    private ClientInterface clientInterface;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }
}
