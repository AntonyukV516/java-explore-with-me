package ru.practicum.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}
