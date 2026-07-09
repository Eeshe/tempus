package me.eeshe.tempus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import me.eeshe.tempus.entity.TimeEntry;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

}
