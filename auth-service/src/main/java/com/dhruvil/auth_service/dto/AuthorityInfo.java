package com.dhruvil.auth_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuthorityInfo {

    private List<String> roles;

    private List<String> permissions;

}