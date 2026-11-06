package me.eeshe.tempus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.UserSettings;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserId(long userId);
}
