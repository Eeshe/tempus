package me.eeshe.tempus.support;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import me.eeshe.tempus.entity.Client;
import me.eeshe.tempus.entity.Project;
import me.eeshe.tempus.entity.Task;
import me.eeshe.tempus.entity.TimeEntry;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.repository.ClientRepository;
import me.eeshe.tempus.repository.ProjectRepository;
import me.eeshe.tempus.repository.TaskRepository;
import me.eeshe.tempus.repository.TimeEntryRepository;
import me.eeshe.tempus.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class RepositoryTestBase {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ClientRepository clientRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected TimeEntryRepository timeEntryRepository;

    @Autowired
    protected TestEntityManager entityManager;

    protected User createUser(String name) {
        return userRepository.save(new User(name, "MyPassword"));
    }

    protected Client createClient(String name, User user) {
        return clientRepository.save(new Client(name, user));
    }

    protected Project createProject(String name, User user) {
        return createProject(name, user, null, null);
    }

    protected Project createProject(String name, User user, BigDecimal hourlyRate, Client client) {
        return projectRepository.save(new Project(name, user, hourlyRate, client));
    }

    protected Task createTask(String name, User user, Project project) {
        return taskRepository.save(new Task(name, user, project));
    }

    protected TimeEntry createTimeEntry(
            User user,
            Project project,
            Task task,
            String description,
            boolean isBillable,
            Instant startTime,
            Instant endTime) {
        return timeEntryRepository.save(new TimeEntry(
                user,
                project,
                task,
                description,
                isBillable,
                startTime,
                endTime));
    }
}
