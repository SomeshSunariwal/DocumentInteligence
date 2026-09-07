package com.example.doc_intel.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaEventDTO<T> {
    private UUID id;
    private T message;
}
