package com.example.doc_intel.Entity;

import com.example.doc_intel.Enums.ChatModelType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private ChatModelType type;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private String baseURL;
    
    @Column(nullable = false)
    private String apiKey;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_email", referencedColumnName = "user_email", nullable = false)
    private UserEntity user;
}
