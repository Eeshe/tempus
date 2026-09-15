package me.eeshe.tempus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserIdAndName(long userId, String name);
}
