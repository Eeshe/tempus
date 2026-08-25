package me.eeshe.tempus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserId(long userId);
}
