package me.eeshe.tempus.mapper;

import me.eeshe.tempus.dto.CreateTaskRequestDTO;
import me.eeshe.tempus.dto.PatchTaskRequestDTO;
import me.eeshe.tempus.dto.TaskDTO;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.request.CreateTaskRequest;
import me.eeshe.tempus.request.PatchTaskRequest;

public interface TaskMapper {

    TaskDTO toDTO(Task task);

    CreateTaskRequest fromDTO(CreateTaskRequestDTO createTaskRequestDTO, long userId);

    PatchTaskRequest fromDTO(PatchTaskRequestDTO patchTaskRequestDTO);
}
