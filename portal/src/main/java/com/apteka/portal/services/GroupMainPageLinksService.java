package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.apteka.portal.components.TypeNameValidator;
import com.apteka.portal.dtos.request.GroupMainPageLinksRequestDTO;
import com.apteka.portal.dtos.response.GroupMainPageLinksResponseDTO;
import com.apteka.portal.exceptions.GroupMainPageLinksAlreadyExistsException;
import com.apteka.portal.exceptions.GroupMainPageLinksNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupMainPageLinksDescriptionException;
import com.apteka.portal.exceptions.InvalidGroupMainPageLinksNameException;
import com.apteka.portal.models.GroupMainPageLinks;
import com.apteka.portal.repository.GroupMainPageLinksRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMainPageLinksService {
    private final GroupMainPageLinksRepository groupMainPageLinksRepository;
    private final TypeNameValidator typeNameValidator;

    public List<GroupMainPageLinksResponseDTO> getAll() {
        return groupMainPageLinksRepository.findAll()
                .stream().map(GroupMainPageLinksResponseDTO::from).toList();
    }

    public GroupMainPageLinksResponseDTO getOne(Integer id) {
        return GroupMainPageLinksResponseDTO.from(groupMainPageLinksRepository.findById(id)
                .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена")));
    }

    public GroupMainPageLinksResponseDTO create(GroupMainPageLinksRequestDTO dto) {
        String cleanName = validateName(dto.name());
        String cleanDescription = validateDescription(dto.description());
        GroupMainPageLinks saved = groupMainPageLinksRepository.save(GroupMainPageLinks.builder()
            .name(cleanName)
            .description(cleanDescription)
            .build());

        return GroupMainPageLinksResponseDTO.from(saved);
    }

    public GroupMainPageLinksResponseDTO update(Integer id, GroupMainPageLinksRequestDTO dto) {
        GroupMainPageLinks groupMainPageLinks = groupMainPageLinksRepository.findById(id)
            .orElseThrow(() -> new GroupMainPageLinksNotFoundException("Группа ссылок не найдена"));
        if (dto.name() != null && !Objects.equals(typeNameValidator.getCleanName(groupMainPageLinks.getName()), dto.name())) {
            groupMainPageLinks.setName(validateName(dto.name()));
        }
        if (dto.description() != null && !Objects.equals(typeNameValidator.getCleanName(groupMainPageLinks.getDescription()), dto.description())) {
            groupMainPageLinks.setDescription(validateDescription(dto.description()));
        }
        return GroupMainPageLinksResponseDTO.from(groupMainPageLinks);
    }

    public void delete(Integer id) {
        if (!groupMainPageLinksRepository.existsById(id)) {
            throw new GroupMainPageLinksNotFoundException("Группа ссылок не найдена");
        }
        groupMainPageLinksRepository.deleteById(id);
    }

    private String validateName(String name) {
        name = typeNameValidator.getCleanName(name);
        
        if (name.length() > 50) {
            throw new InvalidGroupMainPageLinksNameException("Наименование группы ссылок не может быть больше 50 символов!");
        }

        if (name.isBlank()) {
            throw new InvalidGroupMainPageLinksNameException("Наименование группы ссылок не может быть пустым!");
        }

        if (groupMainPageLinksRepository.existsByName(name)) {
            throw new GroupMainPageLinksAlreadyExistsException("Группа " + name + " уже существует!");
        }

        return name;
    }

    private String validateDescription(String description) {

        description = typeNameValidator.getCleanName(description);

        if (description.length() > 100) {
            throw new InvalidGroupMainPageLinksDescriptionException("Описание группы ссылок не может быть больше 100 символов!");
        }
        if (description.isEmpty()) {
            throw new InvalidGroupMainPageLinksDescriptionException("Описание группы ссылок не может быть пустым, но может быть не указано!");
        }

        return description;
    }
}
