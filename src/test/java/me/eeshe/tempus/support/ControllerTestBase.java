package me.eeshe.tempus.support;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.security.UserDetailsImpl;

public abstract class ControllerTestBase {

    @Autowired
    protected MockMvcTester mockMvc;

    @MockitoBean
    protected UserRepository userRepository;

    protected UserDetailsImpl createPrincipal(long userId) {
        return new UserDetailsImpl(createTestUser(userId));
    }

    protected User createTestUser(long userId) {
        final User user = new User("MyUser", "MyPassword");
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    protected UserDTO createTestUserDTO(long userId, Instant createdAt) {
        return new UserDTO(userId, "MyUser", createdAt);
    }
}
