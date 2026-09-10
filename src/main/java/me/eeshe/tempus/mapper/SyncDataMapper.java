package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.SyncDataDTO;
import me.eeshe.tempus.model.SyncData;

public interface SyncDataMapper {

    SyncDataDTO toDTO(SyncData syncData);
}
