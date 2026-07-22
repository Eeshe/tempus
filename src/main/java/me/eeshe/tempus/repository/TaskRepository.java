package me.eeshe.tempus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
