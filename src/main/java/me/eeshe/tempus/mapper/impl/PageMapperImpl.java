package me.eeshe.tempus.mapper.impl;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.PageDTO;
import me.eeshe.tempus.mapper.PageMapper;

@Component
public class PageMapperImpl implements PageMapper {

    @Override
    public <T> PageDTO<T> toDTO(Page<T> page) {
        return new PageDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
