package org.kiwiproject.champagne.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.champagne.dao.ReleaseDao;
import org.kiwiproject.champagne.dao.ReleaseStatusDao;
import org.kiwiproject.champagne.dao.TaskDao;
import org.kiwiproject.champagne.dao.TaskStatusDao;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;

@DisplayName("ManualTaskService")
class ManualTaskServiceTest {
    
    private ReleaseDao releaseDao;
    private ReleaseStatusDao releaseStatusDao;
    private TaskDao taskDao;
    private TaskStatusDao taskStatusDao;
    private ManualTaskService service;
    
    @BeforeEach
    void setUp() {
        releaseDao = mock(ReleaseDao.class);
        releaseStatusDao = mock(ReleaseStatusDao.class);
        taskDao = mock(TaskDao.class);
        taskStatusDao = mock(TaskStatusDao.class);

        service = new ManualTaskService(releaseDao, releaseStatusDao, taskDao, taskStatusDao);
    }

    @Nested
    class AddManualReleaseAndTaskStatusForNewEnv {

        @Test
        void shouldCreateNewNotRequiredStatusesForEachReleaseAndTask() {
            when(releaseDao.findAllReleaseIds()).thenReturn(List.of(1L));
            when(taskDao.findAllTaskIds()).thenReturn(List.of(2L));

            service.addManualReleaseAndTaskStatusForNewEnv(3L);

            verify(releaseDao).findAllReleaseIds();
            verify(taskDao).findAllTaskIds();
            verify(releaseStatusDao).insertReleaseStatus(argThat(status -> 3 == status.getEnvironmentId() && 1 == status.getReleaseId() && DeploymentTaskStatus.NOT_REQUIRED == status.getStatus()));
            verify(taskStatusDao).insertTaskStatus(argThat(status -> 3 == status.getEnvironmentId() && 2 == status.getTaskId() && DeploymentTaskStatus.NOT_REQUIRED == status.getStatus()));

            verifyNoMoreInteractions(releaseDao, taskDao, releaseStatusDao, taskStatusDao);
        }
    }
}
