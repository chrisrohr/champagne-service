package org.kiwiproject.champagne.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.Release;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.core.manualdeployment.Task;
import org.kiwiproject.champagne.core.manualdeployment.TaskStatus;
import org.kiwiproject.champagne.jdbi.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.jdbi.ReleaseDao;
import org.kiwiproject.champagne.jdbi.ReleaseStatusDao;
import org.kiwiproject.champagne.jdbi.TaskDao;
import org.kiwiproject.champagne.jdbi.TaskStatusDao;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.spring.data.KiwiPage;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

@DisplayName("TaskResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class TaskResourceTest {

    private static final ReleaseDao RELEASE_DAO = mock(ReleaseDao.class);
    private static final ReleaseStatusDao RELEASE_STATUS_DAO = mock(ReleaseStatusDao.class);
    private static final TaskDao TASK_DAO = mock(TaskDao.class);
    private static final TaskStatusDao TASK_STATUS_DAO = mock(TaskStatusDao.class);
    private static final DeploymentEnvironmentDao DEPLOYMENT_ENVIRONMENT_DAO = mock(DeploymentEnvironmentDao.class);

    private static final TaskResource RESOURCE = new TaskResource(RELEASE_DAO, RELEASE_STATUS_DAO, TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
        .bootstrapLogging(false)
        .addResource(RESOURCE)
        .addProvider(JerseyViolationExceptionMapper.class)
        .addProvider(JaxrsExceptionMapper.class)
        .build();

    @AfterEach
    void cleanup() {
        reset(RELEASE_DAO, RELEASE_STATUS_DAO, TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
    }

    @Nested
    class GetPagedReleases {

        @Test
        void shouldReturnPagedListOfReleases() {
            var release = Release.builder()
                .id(1L)
                .releaseNumber("2023.42")
                .build();

            when(RELEASE_DAO.findPagedReleases(0, 10)).thenReturn(List.of(release));
            when(RELEASE_DAO.countReleases()).thenReturn(1L);

            var releaseStatus = ReleaseStatus.builder()
                .releaseId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .environmentId(1L)
                .build();

            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases")
                .queryParam("pageNumber", 1)
                .queryParam("pageSize", 10)
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<ReleaseWithStatus>>(){});

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            var releaseWithStatus = first(result.getContent());
            assertThat(releaseWithStatus.getId()).isOne();
            assertThat(releaseWithStatus.getReleaseNumber()).isEqualTo("2023.42");
            assertThat(releaseWithStatus.getEnvironmentStatus()).contains(entry(1L, DeploymentTaskStatus.PENDING));

            verify(RELEASE_DAO).findPagedReleases(0, 10);
            verify(RELEASE_DAO).countReleases();
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);

            verifyNoMoreInteractions(RELEASE_DAO, RELEASE_STATUS_DAO);
            verifyNoInteractions(TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
        }

        @Test
        void shouldReturnPagedListOfReleasesWithDefaultPaging() {
            var release = Release.builder()
                .id(1L)
                .releaseNumber("2023.42")
                .build();

            when(RELEASE_DAO.findPagedReleases(0, 50)).thenReturn(List.of(release));
            when(RELEASE_DAO.countReleases()).thenReturn(1L);

            var releaseStatus = ReleaseStatus.builder()
                .releaseId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .environmentId(1L)
                .build();

            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases")
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<ReleaseWithStatus>>(){});

            assertThat(result.getNumber()).isOne();
            assertThat(result.getSize()).isEqualTo(50);
            assertThat(result.getTotalElements()).isOne();

            var releaseWithStatus = first(result.getContent());
            assertThat(releaseWithStatus.getId()).isOne();
            assertThat(releaseWithStatus.getReleaseNumber()).isEqualTo("2023.42");
            assertThat(releaseWithStatus.getEnvironmentStatus()).contains(entry(1L, DeploymentTaskStatus.PENDING));

            verify(RELEASE_DAO).findPagedReleases(0, 50);
            verify(RELEASE_DAO).countReleases();
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);

            verifyNoMoreInteractions(RELEASE_DAO, RELEASE_STATUS_DAO);
            verifyNoInteractions(TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
        }
    }
    
    @Nested
    class GetTasksForRelease {

        @Test
        void shouldReturnTasks() {
            var task = Task.builder()
                .id(1L)
                .releaseId(1L)
                .component("ZK")
                .summary("Upgrade")
                .description("All the steps")
                .stage(ReleaseStage.PRE)
                .build();

            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task));

            var taskStatus = TaskStatus.builder()
                .taskId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .environmentId(1L)
                .build();

            when(TASK_STATUS_DAO.findByTaskId(1L)).thenReturn(List.of(taskStatus));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases/{releaseId}")
                .resolveTemplate("releaseId", 1L)
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<List<TaskWithStatus>>() {});

            var taskWithStatus = first(result);

            assertThat(taskWithStatus.getId()).isOne();
            assertThat(taskWithStatus.getComponent()).isEqualTo(task.getComponent());
            assertThat(taskWithStatus.getSummary()).isEqualTo(task.getSummary());
            assertThat(taskWithStatus.getDescription()).isEqualTo(task.getDescription());
            assertThat(taskWithStatus.getStage()).isEqualTo(task.getStage());
            assertThat(taskWithStatus.getEnvironmentStatus()).contains(entry(1L, DeploymentTaskStatus.PENDING));

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(1L);

            verifyNoMoreInteractions(TASK_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO, RELEASE_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
        }
    }
    
    
}
