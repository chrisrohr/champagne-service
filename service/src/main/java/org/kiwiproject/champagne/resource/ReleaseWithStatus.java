package org.kiwiproject.champagne.resource;

import java.util.Map;

import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStatus;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

/**
 * This model is used for the {@link TaskResource} to combine the release information with all of the statues for the stored environments.
 */
@Value
@Builder
class ReleaseWithStatus {

    @Delegate
    Release release;

    Map<Long, ReleaseStatus> environmentStatus;
}
