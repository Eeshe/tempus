package me.eeshe.tempus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

}
