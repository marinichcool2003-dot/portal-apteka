package com.apteka.portal.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.services.UserGroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/user-groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserGroupService userGroupService;
}
