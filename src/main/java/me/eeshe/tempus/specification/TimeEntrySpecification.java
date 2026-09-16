package me.eeshe.tempus.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.PredicateSpecification;

import jakarta.persistence.criteria.Predicate;
import me.eeshe.tempus.entity.TimeEntry;

public class TimeEntrySpecification {

    public static PredicateSpecification<TimeEntry> withFilters(
            LocalDate startDate,
            LocalDate endDate,
            List<Long> projectIds,
            List<Long> taskIds,
            List<Long> clientIds,
            List<String> descriptions,
            Boolean isBillable) {
        return (from, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isNotNull(from.get("endTime")));

            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(from.get("startTime"), startDate.atTime(0, 0)));
                predicates.add(criteriaBuilder.lessThanOrEqualTo(from.get("endTime"), endDate.atTime(23, 59)));
            }
            if (projectIds != null && !projectIds.isEmpty()) {
                predicates.add(from.get("project").get("id").in(projectIds));
            }
            if (taskIds != null && !taskIds.isEmpty()) {
                predicates.add(from.get("task").get("id").in(taskIds));
            }
            if (clientIds != null && !clientIds.isEmpty()) {
                predicates.add(from.get("project").get("client").isNotNull());
                predicates.add(from.get("project").get("client").get("id").in(clientIds));
            }
            if (descriptions != null && !descriptions.isEmpty()) {
                predicates.add(from.get("description").in(descriptions));
            }
            if (isBillable != null) {
                predicates.add(criteriaBuilder.equal(from.get("isBillable"), isBillable));
            }
            return criteriaBuilder.and(predicates);
        };
    }
}
