package com.apteka.portal.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.apteka.portal.dtos.request.GroupAptekiRequestDTO;
import com.apteka.portal.models.GroupApteki;
import com.apteka.portal.services.GroupAptekiService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; 
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GroupAptekiControllerTest {
    @Mock
    private GroupAptekiService groupAptekiService;

    @InjectMocks
    private GroupAptekiController groupAptekiController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(groupAptekiController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAll_ShouldReturnList() throws Exception{
        GroupApteki g1 = new GroupApteki(1, "Группа 1");
        GroupApteki g2 = new GroupApteki(2, "Группа 2");

        when(groupAptekiService.getAll()).thenReturn(List.of(g1, g2));

        mockMvc.perform(get("/api/v1/group-apteki"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Группа 1"))
                .andExpect(jsonPath("$[1].name").value("Группа 2"));

        verify(groupAptekiService).getAll();
    }

   @Test
    void getOne_ShouldReturnGroupById() throws Exception {
        GroupApteki group = new GroupApteki(1, "Аптеки Центр");

        when(groupAptekiService.getOne(1)).thenReturn(group);

        mockMvc.perform(get("/api/v1/group-apteki/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Аптеки Центр"));

        verify(groupAptekiService).getOne(1);
    }

    @SuppressWarnings("null")
    @Test
    void create_ShouldReturnCreatedGroup() throws Exception {
        GroupAptekiRequestDTO request = new GroupAptekiRequestDTO("Новая группа");
        GroupApteki saved = new GroupApteki(10, "Новая группа");

        when(groupAptekiService.create("Новая группа")).thenReturn(saved);

        mockMvc.perform(post("/api/v1/group-apteki")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Новая группа"));

        verify(groupAptekiService).create("Новая группа");
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(groupAptekiService).delete(1);

        mockMvc.perform(delete("/api/v1/group-apteki/1"))
                .andExpect(status().isNoContent());

        verify(groupAptekiService).delete(1);
    }

}
