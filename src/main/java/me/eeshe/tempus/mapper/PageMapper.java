package me.eeshe.tempus.mapper;

import org.springframework.data.domain.Page;

import me.eeshe.tempus.dto.PageDTO;

public interface PageMapper {

    <T> PageDTO<T> toDTO(Page<T> page);
}
