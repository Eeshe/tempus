package me.eeshe.tempus.dto;

import java.util.List;
import java.util.Optional;

public record PatchGroupRequestDTO(
        Optional<String> name,
        Optional<List<Long>> userIds) {
}
