package me.eeshe.tempus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.eeshe.tempus.entity.TimeEntry;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

}
