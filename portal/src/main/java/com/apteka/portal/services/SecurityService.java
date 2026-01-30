package com.apteka.portal.services;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.apteka.portal.models.Client;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService {
    
    private final ClientService clientService;
    
    public boolean isOwner(UUID clientId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            Client targetClient = clientService.getOne(clientId);
            return targetClient.getUsername().equals(currentUsername);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean canAccessClient(UUID clientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // LEGEND и ADMIN могут видеть всех
        if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_LEGEND") || 
                          a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // Проверяем, является ли пользователь владельцем
        return isOwner(clientId);
    }
}