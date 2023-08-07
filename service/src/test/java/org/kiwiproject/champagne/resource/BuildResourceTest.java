package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.constants.KiwiTestConstants.JSON_HELPER;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.BuildDao;
import org.kiwiproject.champagne.junit.jupiter.DeployableSystemExtension;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.spring.data.KiwiPage;

@DisplayName("BuildResource")
@ExtendWith({DropwizardExtensionsSupport.class, DeployableSystemExtension.class})
class BuildResourceTest {

    private static final BuildDao BUILD_DAO = mock(BuildDao.class);

    private static final BuildResource RESOURCE = new BuildResource(BUILD_DAO, JSON_HELPER);

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(RESOURCE)
            .addProvider(JerseyViolationExceptionMapper.class)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    public final JwtExtension jwtExtension = new JwtExtension("bob");

    @AfterEach
    void cleanup() {
        reset(BUILD_DAO);
    }

    @Nested
    class GetPagedReleases {

        @Test
        void shouldReturnPagedListOfReleases() {
            var build = Build.builder()
                    .repoNamespace("kiwiproject")
                    .repoName("champagne-service")
                    .commitRef("abc1234")
                    .commitUser("jdoe")
                    .sourceBranch("main")
                    .componentIdentifier("champagne_service")
                    .componentVersion("42.0.0")
                    .distributionLocation("https://some-nexus-server.net/foo")
                    .extraDeploymentInfo(Map.of())
                    .deployableSystemId(1L)
                    .build();

            when(BUILD_DAO.findPagedBuilds(0, 10, 1L, null)).thenReturn(List.of(build));
            when(BUILD_DAO.countBuilds(1L, null)).thenReturn(1L);

            var response = RESOURCES.client()
                    .target("/build")
                    .queryParam("pageNumber", 1)
                    .queryParam("pageSize", 10)
                    .request()
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<Build>>() {
            });

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            verify(BUILD_DAO).findPagedBuilds(0, 10, 1L, null);
            verify(BUILD_DAO).countBuilds(1L, null);

            verifyNoMoreInteractions(BUILD_DAO);
        }

        @Test
        void shouldReturnPagedListOfReleasesWithDefaultPaging() {
            var build = Build.builder()
                    .repoNamespace("kiwiproject")
                    .repoName("champagne-service")
                    .commitRef("abc1234")
                    .commitUser("jdoe")
                    .sourceBranch("main")
                    .componentIdentifier("champagne_service")
                    .componentVersion("42.0.0")
                    .distributionLocation("https://some-nexus-server.net/foo")
                    .extraDeploymentInfo(Map.of())
                    .deployableSystemId(1L)
                    .build();

            when(BUILD_DAO.findPagedBuilds(0, 50, 1L, null)).thenReturn(List.of(build));
            when(BUILD_DAO.countBuilds(1L, null)).thenReturn(1L);

            var response = RESOURCES.client()
                    .target("/build")
                    .request()
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<ReleaseWithStatus>>() {
            });

            assertThat(result.getNumber()).isOne();
            assertThat(result.getSize()).isEqualTo(50);
            assertThat(result.getTotalElements()).isOne();

            verify(BUILD_DAO).findPagedBuilds(0, 50, 1L, null);
            verify(BUILD_DAO).countBuilds(1L, null);

            verifyNoMoreInteractions(BUILD_DAO);
        }
    }

    @Nested
    class AddNewBuild {

        @Test
        void shouldSaveNewBuild() {
            var build = Build.builder()
                    .repoNamespace("kiwiproject")
                    .repoName("champagne-service")
                    .commitRef("abc1234")
                    .commitUser("jdoe")
                    .sourceBranch("main")
                    .componentIdentifier("champagne_service")
                    .componentVersion("42.0.0")
                    .distributionLocation("https://some-nexus-server.net/foo")
                    .extraDeploymentInfo(Map.of())
                    .deployableSystemId(1L)
                    .build();

            when(BUILD_DAO.insertBuild(any(Build.class), anyString())).thenReturn(1L);

            var response = RESOURCES.client()
                    .target("/build")
                    .request()
                    .post(json(build));

            assertAcceptedResponse(response);

            verify(BUILD_DAO).insertBuild(any(Build.class), anyString());

            verifyNoMoreInteractions(BUILD_DAO);
        }

        @Test
        void shouldSaveNewBuildWithCurrentSystemWhenNotProvided() {
            var build = Build.builder()
                    .repoNamespace("kiwiproject")
                    .repoName("champagne-service")
                    .commitRef("abc1234")
                    .commitUser("jdoe")
                    .sourceBranch("main")
                    .componentIdentifier("champagne_service")
                    .componentVersion("42.0.0")
                    .distributionLocation("https://some-nexus-server.net/foo")
                    .extraDeploymentInfo(Map.of())
                    .build();

            when(BUILD_DAO.insertBuild(any(Build.class), anyString())).thenReturn(1L);

            var response = RESOURCES.client()
                    .target("/build")
                    .request()
                    .post(json(build));

            assertAcceptedResponse(response);

            verify(BUILD_DAO).insertBuild(any(Build.class), anyString());

            verifyNoMoreInteractions(BUILD_DAO);
        }
    }
}
