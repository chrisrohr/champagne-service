package org.kiwiproject.champagne.resource;

import java.util.Map;

import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.Task;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

/**
 * This model is used for the {@link TaskResource} to combine the task information with all of the statues for the stored environments.
 */
@Value
@Builder
class TaskWithStatus {
    
    @Delegate
    Task task;

    Map<Long, DeploymentTaskStatus> environmentStatus;
}
