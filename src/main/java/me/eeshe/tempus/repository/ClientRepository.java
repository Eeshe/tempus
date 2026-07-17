package me.eeshe.tempus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

}
