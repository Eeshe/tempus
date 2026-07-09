package me.eeshe.tempus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

}
