package me.eeshe.tempus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByUserIdAndNameAndProjectId(long userId, String name, long projectId);
}
