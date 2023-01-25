package org.kiwiproject.champagne.resource;

import java.util.Map;

import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.Release;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

/**
 * This model is used for the {@link TaskResource.class} to combine the release information with all of the statues for the stored environments.
 */
@Value
@Builder
class ReleaseWithStatus {

    @Delegate
    Release release;

    Map<Long, DeploymentTaskStatus> environmentStatus;
}
