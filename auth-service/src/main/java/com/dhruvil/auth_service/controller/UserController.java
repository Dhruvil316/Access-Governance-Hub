package com.dhruvil.auth_service.controller;

import com.dhruvil.auth_service.dto.ApiResponse;
import com.dhruvil.auth_service.dto.UserCreateRequest;
import com.dhruvil.auth_service.dto.UserResponse;
import com.dhruvil.auth_service.dto.UserSummaryResponse;
import com.dhruvil.auth_service.dto.UserUpdateRequest;
import com.dhruvil.auth_service.security.UserPrincipal;
import com.dhruvil.auth_service.services.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    //  Setting up kafka template
    private final KafkaTemplate<String,String>  kafkaTemplate ;

    //  Testing the Kafka
    @PostMapping("/kafka/{message}")
    public ResponseEntity<String> sendMessage (@PathVariable String message )
    {
        // kafka topic cannot have space in the topic name
        kafkaTemplate.send("user-random-topic", message);
        return ResponseEntity.ok("Message queued") ;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Current user profile fetched successfully.",
                        userManagementService.findByEmail(principal.getEmail())
                )
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> findAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Users fetched successfully.",
                        userManagementService.findAll()
                )
        );
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> search(
            @RequestParam(name = "query", required = false) String query
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Users search completed successfully.",
                        userManagementService.search(query)
                )
        );
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "User fetched successfully.",
                        userManagementService.findById(id)
                )
        );
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "User created successfully.",
                        userManagementService.create(request)
                ));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "User updated successfully.",
                        userManagementService.update(id, request)
                )
        );
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userManagementService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("User deleted successfully.", null));
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Role assigned to user successfully.",
                        userManagementService.assignRole(userId, roleId)
                )
        );
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Role removed from user successfully.",
                        userManagementService.removeRole(userId, roleId)
                )
        );
    }
}
