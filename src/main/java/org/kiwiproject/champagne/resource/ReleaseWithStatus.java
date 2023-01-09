package org.kiwiproject.champagne.resource;

import java.util.Map;

import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.Release;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

@Value
@Builder
public class ReleaseWithStatus {

    @Delegate
    Release release;

    Map<Long, DeploymentTaskStatus> environmentStatus;
}
