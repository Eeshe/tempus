package me.eeshe.tempus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndUserId(long id, long userId);

    List<Client> findByUserId(long userId);

    Optional<Client> findByUserIdAndName(long userId, String name);
}
