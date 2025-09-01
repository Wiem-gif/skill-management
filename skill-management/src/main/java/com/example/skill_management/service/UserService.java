package com.example.skill_management.service;

import com.example.skill_management.dto.CreateUserRequest;
import com.example.skill_management.Enum.ErrorCodeEnum;
import com.example.skill_management.dto.UserResponseDTO;
import com.example.skill_management.exception.EmailAlreadyExistsException;
import com.example.skill_management.exception.ProtectedUserDeletionException;
import com.example.skill_management.exception.RequiredFieldException;
import com.example.skill_management.exception.UserNotFoundException;
import com.example.skill_management.model.User;
import com.example.skill_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<Void> createUser(CreateUserRequest request, String createdBy) {

        if (request.getFirstname() == null || request.getFirstname().isBlank()) {
            return Mono.error(new RequiredFieldException(ErrorCodeEnum.SMGT_USER_CREATE_REQUIRED_FIRSTNAME));
        }
        if (request.getLastname() == null || request.getLastname().isBlank()) {
            return Mono.error(new RequiredFieldException(ErrorCodeEnum.SMGT_USER_CREATE_REQUIRED_LASTNAME));
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return Mono.error(new RequiredFieldException(ErrorCodeEnum.SMGT_USER_CREATE_REQUIRED_EMAIL));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.error(new RequiredFieldException(ErrorCodeEnum.SMGT_USER_CREATE_REQUIRED_PASSWORD));
        }
        if (request.getRole() == null) {
            return Mono.error(new RequiredFieldException(ErrorCodeEnum.SMGT_USER_CREATE_REQUIRED_ROLE));
        }

        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new EmailAlreadyExistsException());
                    }

                    User user = User.builder()
                            .firstname(request.getFirstname())
                            .lastname(request.getLastname())
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .role(request.getRole())
                            .createdBy(createdBy)
                            .creationDate(LocalDateTime.now())
                            .status(true)
                            .build();

                    return userRepository.save(user).then();
                });
    }

    public Mono<Void> deleteUser(Integer id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException()))
                .flatMap(user -> {
                    if (user.isProtected()) {
                        return Mono.error(new ProtectedUserDeletionException());
                    }
                    return userRepository.deleteById(id);
                });
    }

    // 🔹 Compter tous les utilisateurs
    public Mono<Long> countAllUsers() {
        return userRepository.count();
    }

    // 🔹 Récupérer les utilisateurs avec pagination (version DTO)
    public Flux<UserResponseDTO> getAllUsers(int offset, int limit) {
        return userRepository.findAllWithPagination(offset, limit)
                .map(user -> new UserResponseDTO(
                        user.getFirstname(),
                        user.getLastname(),
                        user.getEmail(),
                        user.getRole().name()
                ));
    }

}
