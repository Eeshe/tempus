package me.eeshe.tempus.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import me.eeshe.tempus.config.SecurityConfig;
import me.eeshe.tempus.dto.UserDTO;
import me.eeshe.tempus.entity.User;
import me.eeshe.tempus.repository.UserRepository;
import me.eeshe.tempus.security.RestAuthenticationEntryPoint;
import me.eeshe.tempus.security.UserDetailsImpl;

@Import({ SecurityConfig.class, RestAuthenticationEntryPoint.class })
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvcTester mockMvc;

    @MockitoBean
    protected UserRepository userRepository;

    protected RequestPostProcessor createPrincipal(long userId) {
        return user(new UserDetailsImpl(createUser(userId)));
    }

    protected static User createUser(long userId) {
        final User user = new User("MyUser", "MyPassword");
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    protected static UserDTO createTestUserDTO(long userId, Instant createdAt) {
        return new UserDTO(userId, "MyUser", createdAt);
    }
}
