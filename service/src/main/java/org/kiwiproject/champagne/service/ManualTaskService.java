package org.kiwiproject.champagne.service;

import org.kiwiproject.champagne.dao.ReleaseDao;
import org.kiwiproject.champagne.dao.ReleaseStatusDao;
import org.kiwiproject.champagne.dao.TaskDao;
import org.kiwiproject.champagne.dao.TaskStatusDao;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ManualTaskService {
    
    private final ReleaseDao releaseDao;
    private final ReleaseStatusDao releaseStatusDao;
    private final TaskDao taskDao;
    private final TaskStatusDao taskStatusDao; 

    public void addManualReleaseAndTaskStatusForNewEnv(long envId) {
        releaseDao.findAllReleaseIds().forEach(releaseId -> {
            var status = ReleaseStatus.builder()
                .environmentId(envId)
                .releaseId(releaseId)
                .status(DeploymentTaskStatus.NOT_REQUIRED)
                .build(); 
            
            releaseStatusDao.insertReleaseStatus(status);
        });

        taskDao.findAllTaskIds().forEach(taskId -> {
            var status = TaskStatus.builder()
                .environmentId(envId)
                .taskId(taskId)
                .status(DeploymentTaskStatus.NOT_REQUIRED)
                .build(); 
            
            taskStatusDao.insertTaskStatus(status);
        });
    }
}
