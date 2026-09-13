package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.UserDetailsDTO;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        Optional<UserEntity> userEntityOptional = userRepository.findByEmail(username);
        if (userEntityOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        UserEntity user = userEntityOptional.get();

        return UserDetailsDTO.builder().user(user).build();
    }
}
