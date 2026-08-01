package com.dhruvil.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_roles_user_role",
                        columnNames = {"user_id", "role_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_roles_user", columnList = "user_id"),
                @Index(name = "idx_user_roles_role", columnList = "role_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 user can have many roles
 A role can belong to many users.
*/
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    // many UserRole rows maps to single user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // many UserRole rows maps to single role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
