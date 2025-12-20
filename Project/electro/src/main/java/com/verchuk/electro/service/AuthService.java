package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.LoginRequest;
import com.verchuk.electro.dto.request.RegisterRequest;
import com.verchuk.electro.dto.response.JwtResponse;
import com.verchuk.electro.exception.BadRequestException;
import com.verchuk.electro.model.Role;
import com.verchuk.electro.model.User;
import com.verchuk.electro.repository.RoleRepository;
import com.verchuk.electro.repository.UserRepository;
import com.verchuk.electro.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .build();

        Set<Role> roles = new HashSet<>();
        Role designerRole = roleRepository.findByName(Role.RoleName.DESIGNER)
                .orElseThrow(() -> new BadRequestException("Designer role not found"));
        roles.add(designerRole);
        user.setRoles(roles);

        userRepository.save(user);

        String jwt = jwtUtils.generateToken(user);
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getEmail(), roleNames);
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(user);

        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getEmail(), roleNames);
    }
}

