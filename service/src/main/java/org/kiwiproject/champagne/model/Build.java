package org.kiwiproject.champagne.model;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;

/**
 * Model used to represent the build of a particular deployment artifact.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Build {
    
    private Long id;
    private Instant createdAt;

    /**
     * Indicates whether the build has been released to a promotable environment (e.g. test or production)
     */
    private boolean released;

    // SCM info
    private String repoNamespace;
    private String repoName;
    private String commitRef;
    private String commitUser;
    private String sourceBranch;

    // Artifact info
    private String componentIdentifier;
    private String componentVersion;
    private String distributionLocation;
    private Map<String, Object> extraDeploymentInfo;
}
