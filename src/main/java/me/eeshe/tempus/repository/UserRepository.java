package me.eeshe.tempus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
