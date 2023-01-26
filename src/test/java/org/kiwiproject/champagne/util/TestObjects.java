package org.kiwiproject.champagne.util;

import org.jdbi.v3.core.Handle;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.model.manualdeployment.Task;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestObjects {

    public static long insertAuditRecord(Handle handle) {
        var testAuditRecord = AuditRecord.builder()
            .userSystemIdentifier("jdoe")
            .action(Action.UPDATED)
            .recordType("Task")
            .recordId(1L)
            .build();

        return handle.createUpdate("insert into audit_records (user_system_identifier, action, record_type, record_id) values (:userSystemIdentifier, :action, :recordType, :recordId)")
            .bindBean(testAuditRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }

    public static long insertDeploymentEnvironmentRecord(Handle handle, String name, long userId) {
        var testDeploymentEnvironmentRecord = DeploymentEnvironment.builder()
            .name(name)
            .createdById(userId)
            .updatedById(userId)
            .build();

        return handle.createUpdate("insert into deployment_environments (environment_name, created_by, updated_by) values (:name, :createdById, :updatedById)")
            .bindBean(testDeploymentEnvironmentRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }

    public static long insertUserRecord(Handle handle, String systemIdentifier) {
        return insertUserRecord(handle, systemIdentifier, "John", "Doe");
    }

    public static long insertUserRecord(Handle handle, String systemIdentifier, String firstName, String lastName) {
        return insertUserRecord(handle, systemIdentifier, firstName, lastName, false);
    }

    public static long insertUserRecord(Handle handle, String systemIdentifier, String firstName, String lastName, boolean deleted) {
        var testUserRecord = User.builder()
            .systemIdentifier(systemIdentifier)
            .firstName(firstName)
            .lastName(lastName)
            .displayName(firstName + " " + lastName)
            .deleted(deleted)
            .build();

        return handle.createUpdate("insert into users (system_identifier, first_name, last_name, display_name, deleted) values (:systemIdentifier, :firstName, :lastName, :displayName, :deleted)")
            .bindBean(testUserRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }

    public static long insertReleaseRecord(Handle handle, String releaseNumber) {
        var testReleaseRecord = Release.builder()
            .releaseNumber(releaseNumber)
            .build();

        return handle.createUpdate("insert into manual_deployment_task_releases (release_number) values (:releaseNumber)")
            .bindBean(testReleaseRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }

    public static long insertReleaseStatusRecord(Handle handle, DeploymentTaskStatus status) {
        var releaseId = insertReleaseRecord(handle, "42");
        return insertReleaseStatusRecord(handle, status, releaseId);
    }

    public static long insertReleaseStatusRecord(Handle handle, DeploymentTaskStatus status, long releaseId) {
        var userId = insertUserRecord(handle, "jdoe");
        var envId = insertDeploymentEnvironmentRecord(handle, "DEV", userId);

        var testReleaseStatusRecord = ReleaseStatus.builder()
            .releaseId(releaseId)
            .environmentId(envId)
            .status(status)
            .build();

        return handle.createUpdate("insert into manual_deployment_task_release_statuses (manual_deployment_task_release_id, deployment_environment_id, status) values (:releaseId, :environmentId, :status)")
            .bindBean(testReleaseStatusRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }

    public static long insertTaskRecord(Handle handle, String summary) {
        var releaseId = insertReleaseRecord(handle, "42");
        return insertTaskRecord(handle, summary, releaseId);
    }

    public static long insertTaskRecord(Handle handle, String summary, long releaseId) {
        var testTaskRecord = Task.builder()
            .releaseId(releaseId)
            .stage(ReleaseStage.POST)
            .summary(summary)
            .component("component")
            .build();

        return handle.createUpdate("insert into manual_deployment_tasks (manual_deployment_task_release_id, stage, summary, component) values (:releaseId, :stage, :summary, :component)")
            .bindBean(testTaskRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }

    public static long insertTaskStatusRecord(Handle handle, DeploymentTaskStatus status, long taskId) {
        var userId = insertUserRecord(handle, "jdoe");
        var envId = insertDeploymentEnvironmentRecord(handle, "DEV", userId);

        var testTaskStatusRecord = TaskStatus.builder()
            .taskId(taskId)
            .environmentId(envId)
            .status(status)
            .build();

        return handle.createUpdate("insert into manual_deployment_task_statuses (manual_deployment_task_id, deployment_environment_id, status) values (:taskId, :environmentId, :status)")
            .bindBean(testTaskStatusRecord)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .first();
    }
    
}
