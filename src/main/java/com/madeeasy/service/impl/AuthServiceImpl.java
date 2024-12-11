package com.madeeasy.service.impl;

import com.madeeasy.dto.request.AuthRequest;
import com.madeeasy.dto.request.LogOutRequest;
import com.madeeasy.dto.request.SignInRequestDTO;
import com.madeeasy.dto.request.UserRequest;
import com.madeeasy.dto.response.AuthResponse;
import com.madeeasy.entity.*;
import com.madeeasy.exception.TokenException;
import com.madeeasy.exception.UsernameNotFoundException;
import com.madeeasy.repository.AddressRepository;
import com.madeeasy.repository.TokenRepository;
import com.madeeasy.repository.UserRepository;
import com.madeeasy.service.AuthService;
import com.madeeasy.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final AddressRepository addressRepository;

    @Override
    public AuthResponse singUp(AuthRequest authRequest) {
        List<String> authRequestRoles = authRequest.getRoles();

        // Convert all roles to uppercase
        List<String> normalizedRoles = authRequestRoles.stream()
                .map(String::toUpperCase) // Convert each role to uppercase
                .toList();

        // Check if roles contain valid enum names
        if (!normalizedRoles.contains(Role.ADMIN.name()) && !normalizedRoles.contains(Role.STUDENT.name())) {
            return AuthResponse.builder()
                    .message("Invalid roles provided. Allowed roles are ADMIN and USER.")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        List<Role> roles = new ArrayList<>();
        if (normalizedRoles.size() == 1) {
            if (normalizedRoles.contains(Role.ADMIN.name())) {
                roles.add(Role.ADMIN);
            }
            if (normalizedRoles.contains(Role.STUDENT.name())) {
                roles.add(Role.STUDENT);
            }
        } else {
            roles.addAll(Arrays.asList(Role.ADMIN, Role.STUDENT));
        }


        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .fullName(authRequest.getFullName())
                .email(authRequest.getEmail())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .phone(authRequest.getPhone())
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .role(roles)
                .build();


        // Check if a user with the given email or phone already exists
        boolean emailExists = userRepository.existsByEmail(authRequest.getEmail());
        boolean phoneExists = userRepository.existsByPhone(authRequest.getPhone());

        if (emailExists && phoneExists) {
            return AuthResponse.builder()
                    .message("User with Email: " + authRequest.getEmail() + " and Phone: " + authRequest.getPhone() + " already exists.")
                    .status(HttpStatus.CONFLICT)
                    .build();
        } else if (emailExists) {
            return AuthResponse.builder()
                    .message("User with Email: " + authRequest.getEmail() + " already exists.")
                    .status(HttpStatus.CONFLICT)
                    .build();
        } else if (phoneExists) {
            return AuthResponse.builder()
                    .message("User with Phone: " + authRequest.getPhone() + " already exists.")
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole().stream().map(Enum::name).toList());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getRole().stream().map(Enum::name).toList());

        Token token = Token.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .token(accessToken)
                .isRevoked(false)
                .isExpired(false)
                .tokenType(TokenType.BEARER)
                .build();


        User savedUser = userRepository.save(user);

        Address address
                = Address.builder()
                .id(UUID.randomUUID().toString())
                .pin(authRequest.getAddressRequestDTO().getPin())
                .city(authRequest.getAddressRequestDTO().getCity())
                .state(authRequest.getAddressRequestDTO().getState())
                .country(authRequest.getAddressRequestDTO().getCountry())
                .user(savedUser)
                .build();

        addressRepository.save(address);

        tokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    @Override
    public AuthResponse singIn(SignInRequestDTO signInRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
            revokeAllPreviousValidTokens(user);
            String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole().stream().map(role -> role.name()).collect(Collectors.toList()));
            String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getRole().stream().map(role -> role.name()).collect(Collectors.toList()));


            Token token = Token.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .token(accessToken)
                    .isRevoked(false)
                    .isExpired(false)
                    .tokenType(TokenType.BEARER)
                    .build();

            tokenRepository.save(token);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            throw new BadCredentialsException("Bad Credential Exception !!");
        }
    }

    @Override
    public void revokeAllPreviousValidTokens(User user) {
        List<Token> tokens = tokenRepository.findAllValidTokens(user.getId());
        tokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(tokens);
    }

    @Override
    public void logOut(LogOutRequest logOutRequest) {
        String email = logOutRequest.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        jwtUtils.validateToken(logOutRequest.getAccessToken(), jwtUtils.getUserName(logOutRequest.getAccessToken()));
        revokeAllPreviousValidTokens(user);
    }

    @Override
    public boolean validateAccessToken(String accessToken) {

        Token token = tokenRepository.findByToken(accessToken).orElseThrow(() -> new TokenException("Token Not found"));

        if (token.isExpired() && token.isRevoked()) {
            throw new TokenException("Token is expired or revoked");
        }
        return !token.isExpired() && !token.isRevoked();
    }

    @Override
    public AuthResponse partiallyUpdateUser(String emailId, UserRequest userRequest) {
        User user = userRepository.findByEmail(emailId).orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        if (userRequest.getFullName() != null) {
            user.setFullName(userRequest.getFullName());
        }


        boolean emailExists = false;
        boolean phoneExists = false;

        // Check if the new email already exists and belongs to another user
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(user.getEmail())) {
            emailExists = userRepository.existsByEmail(userRequest.getEmail());
        }

        // Check if the new phone number already exists and belongs to another user
        if (userRequest.getPhone() != null && !userRequest.getPhone().equals(user.getPhone())) {
            phoneExists = userRepository.existsByPhone(userRequest.getPhone());
        }

        // Handle the case where both email and phone already exist
        if (emailExists && phoneExists) {
            return AuthResponse.builder()
                    .status(HttpStatus.CONFLICT)
                    .message("User with Email: " + userRequest.getEmail() + " and Phone: " + userRequest.getPhone() + " already exist.")
                    .build();
        }

        // Handle the case where only email exists
        if (emailExists) {
            return AuthResponse.builder()
                    .status(HttpStatus.CONFLICT)
                    .message("User with Email: " + userRequest.getEmail() + " already exists.")
                    .build();
        }

        // Handle the case where only phone exists
        if (phoneExists) {
            return AuthResponse.builder()
                    .status(HttpStatus.CONFLICT)
                    .message("User with Phone: " + userRequest.getPhone() + " already exists.")
                    .build();
        }

        if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
            user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPhone() != null && !userRequest.getPhone().isBlank()) {
            user.setPhone(userRequest.getPhone());
        }
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            // Convert all roles to uppercase
            List<String> normalizedRoles = userRequest.getRoles().stream()
                    .map(String::toUpperCase) // Convert each role to uppercase
                    .toList();

            // Validate if roles are valid
            if (!normalizedRoles.contains(Role.ADMIN.name()) && !normalizedRoles.contains(Role.STUDENT.name())) {
                return AuthResponse.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Invalid roles provided. Allowed roles are ADMIN and USER.")
                        .build();
            }

            List<Role> roles = new ArrayList<>();
            if (normalizedRoles.size() == 1) {
                if (normalizedRoles.contains(Role.ADMIN.name())) {
                    roles.add(Role.ADMIN);
                }
                if (normalizedRoles.contains(Role.STUDENT.name())) {
                    roles.add(Role.STUDENT);
                }
            } else {
                roles.addAll(Arrays.asList(Role.ADMIN, Role.STUDENT));
            }
            user.setRole(roles);
        }

        if (userRequest.getAddressPartialRequestDTO() != null) {
            if (userRequest.getAddressPartialRequestDTO().getPin() != null && !userRequest.getAddressPartialRequestDTO().getPin().isBlank()) {
                user.getAddress().setPin(userRequest.getAddressPartialRequestDTO().getPin());
            }
            if (userRequest.getAddressPartialRequestDTO().getCity() != null && !userRequest.getAddressPartialRequestDTO().getCity().isBlank()) {
                user.getAddress().setCity(userRequest.getAddressPartialRequestDTO().getCity());
            }

            if (userRequest.getAddressPartialRequestDTO().getState() != null && !userRequest.getAddressPartialRequestDTO().getState().isBlank()) {
                user.getAddress().setState(userRequest.getAddressPartialRequestDTO().getState());
            }

            if (userRequest.getAddressPartialRequestDTO().getCountry() != null && !userRequest.getAddressPartialRequestDTO().getCountry().isBlank()) {
                user.getAddress().setCountry(userRequest.getAddressPartialRequestDTO().getCountry());
            }
        }

        User savedUser = userRepository.save(user);

        if ((userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) || (userRequest.getEmail() != null && !userRequest.getEmail().isBlank())) {
            revokeAllPreviousValidTokens(savedUser);
            String accessToken = jwtUtils.generateAccessToken(savedUser.getEmail(), savedUser.getRole().stream().map(role -> role.name()).collect(Collectors.toList()));
            String refreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail(), savedUser.getRole().stream().map(role -> role.name()).collect(Collectors.toList()));


            Token token = Token.builder()
                    .id(UUID.randomUUID().toString())
                    .user(savedUser)
                    .token(accessToken)
                    .isRevoked(false)
                    .isExpired(false)
                    .tokenType(TokenType.BEARER)
                    .build();

            tokenRepository.save(token);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        return AuthResponse.builder()
                .status(HttpStatus.OK)
                .message("User updated successfully")
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        boolean isValid = jwtUtils.validateToken(refreshToken, jwtUtils.getUserName(refreshToken));

        if (!isValid) {
            throw new TokenException("Token is invalid");
        }
        User user = userRepository.findByEmail(jwtUtils.getUserName(refreshToken)).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        revokeAllPreviousValidTokens(user);
        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole().stream().map(Enum::name).collect(Collectors.toList()));
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getRole().stream().map(Enum::name).collect(Collectors.toList()));


        Token token = Token.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .token(accessToken)
                .isRevoked(false)
                .isExpired(false)
                .tokenType(TokenType.BEARER)
                .build();

        tokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}