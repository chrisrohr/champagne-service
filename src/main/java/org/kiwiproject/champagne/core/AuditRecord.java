package org.kiwiproject.champagne.core;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Model representation of an audit record used to track user based actions in the
 * system, for instance, creating, updating or deleting objects.
 */
@Builder
@Getter
public class AuditRecord {

    public enum Action {
        CREATED, UPDATED, DELETED
    }

    private final Long id;

    /**
     * The time the action occurred
     */
    private final Instant timestamp;

    /**
     * The user identifier. See {@link User}
     */
    private final String userSystemIdentifier;

    /**
     * The action that was performed resulting in the audit record being created
     */
    private final Action action;

    /**
     * Stores a representation of what type of object was modified. (e.g. a simple class name)
     */
    private final String recordType;

    /**
     * The id field of the object being modified.
     *
     * @implNote This is a long only because there is an assumption that we will only be
     *           auditing objects that are being stored in the database and which uses
     *           longs for the id field.
     */
    private final Long recordId;
}
