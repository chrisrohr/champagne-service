package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertUnauthorizedResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

import java.util.Map;
import java.util.Optional;

@DisplayName("AuthResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class AuthResourceTest {

    private static final UserDao USER_DAO = mock(UserDao.class);
    private static final AuthResource AUTH_RESOURCE = new AuthResource(USER_DAO);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(AUTH_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @BeforeEach
    void setUp() {
        reset(USER_DAO);
    }


    @Nested
    class Login {

        @Test
        void shouldReturn401WhenUserCanNotBeFound() {
            when(USER_DAO.findBySystemIdentifier("jdoe")).thenReturn(Optional.empty());

            var response = APP.client().target("/auth/login")
                    .request()
                    .post(json(Map.of("username", "jdoe")));

            assertUnauthorizedResponse(response);
        }

        @Test
        void shouldReturnJwtAsCookieWhenLoginIsSuccessful() {
            var user = User.builder()
                    .systemIdentifier("jdoe")
                    .build();

            when(USER_DAO.findBySystemIdentifier("jdoe")).thenReturn(Optional.of(user));

            var response = APP.client().target("/auth/login")
                    .request()
                    .post(json(Map.of("username", "jdoe")));

            assertOkResponse(response);
        }

        @Test
        void shouldReturnJwtAsCookieWhenLoginIsSuccessfulAndUserIsAdmin() {
            var user = User.builder()
                    .systemIdentifier("jdoe")
                    .admin(true)
                    .build();

            when(USER_DAO.findBySystemIdentifier("jdoe")).thenReturn(Optional.of(user));

            var response = APP.client().target("/auth/login")
                    .request()
                    .post(json(Map.of("username", "jdoe")));

            assertOkResponse(response);
        }
    }

    @Nested
    class Logout {

        @Test
        void shouldSetCookieToExpireAndReturnNoContent() {
            var response = APP.client().target("/auth/logout")
                    .request()
                    .delete();

            assertNoContentResponse(response);
        }
    }


}
