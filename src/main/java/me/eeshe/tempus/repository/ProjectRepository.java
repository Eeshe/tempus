package me.eeshe.tempus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndUserId(long id, long userId);

    List<Project> findByUserId(long userId);

    Optional<Project> findByUserIdAndName(long userId, String name);
}
