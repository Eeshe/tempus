package me.eeshe.tempus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndUserId(long id, long userId);

    List<Task> findByUserId(long userId);

    Optional<Task> findByUserIdAndNameAndProjectId(long userId, String name, long projectId);
}
