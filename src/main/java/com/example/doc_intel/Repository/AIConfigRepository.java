package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.AIConfig;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface AIConfigRepository extends JpaRepository<AIConfig, Long> {

    Optional<AIConfig> findByUser_UserId(UUID userId);

    Optional<AIConfig> findByUser_Email(@NonNull String Email);
}
