package me.eeshe.tempus.mapper.impl;

import org.springframework.stereotype.Component;

import me.eeshe.tempus.dto.SyncDataDTO;
import me.eeshe.tempus.mapper.SyncDataMapper;
import me.eeshe.tempus.model.SyncData;

@Component
public class SyncDataMapperImpl implements SyncDataMapper {

    @Override
    public SyncDataDTO toDTO(SyncData syncData) {
        return new SyncDataDTO(
                syncData.localSnapshotTime(),
                syncData.remoteSnapshotTime());
    }
}
