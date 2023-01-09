package org.kiwiproject.champagne.resource;

import java.util.Map;

import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.Task;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

@Value
@Builder
public class TaskWithStatus {
    
    @Delegate
    Task task;

    Map<Long, DeploymentTaskStatus> environmentStatus;
}
