package org.kiwiproject.champagne.util;

import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Handle;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.champagne.model.Component;
import org.kiwiproject.champagne.model.DeployableSystem;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.model.GitProvider;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.model.manualdeployment.Task;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;

@UtilityClass
public class TestObjects {

    public static long insertAuditRecord(Handle handle, long systemId) {
        var testAuditRecord = AuditRecord.builder()
                .userSystemIdentifier("jdoe")
                .action(Action.UPDATED)
                .recordType("Task")
                .recordId(1L)
                .deployableSystemId(systemId)
                .build();

        return handle.createUpdate("insert into audit_records (user_system_identifier, action, record_type, record_id, deployable_system_id) values (:userSystemIdentifier, :action, :recordType, :recordId, :deployableSystemId)")
                .bindBean(testAuditRecord)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertDeploymentEnvironmentRecord(Handle handle, String name, long systemId) {
        var testDeploymentEnvironmentRecord = DeploymentEnvironment.builder()
                .name(name)
                .deployableSystemId(systemId)
                .build();

        return handle.createUpdate("insert into deployment_environments (environment_name, deployable_system_id) values (:name, :deployableSystemId)")
                .bindBean(testDeploymentEnvironmentRecord)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertUserRecord(Handle handle, String systemIdentifier) {
        return insertUserRecord(handle, systemIdentifier, "John", "Doe");
    }

    public static long insertUserRecord(Handle handle, String systemIdentifier, String firstName, String lastName) {
        var testUserRecord = User.builder()
                .systemIdentifier(systemIdentifier)
                .firstName(firstName)
                .lastName(lastName)
                .displayName(firstName + " " + lastName)
                .build();

        return handle.createUpdate("insert into users (system_identifier, first_name, last_name, display_name) values (:systemIdentifier, :firstName, :lastName, :displayName)")
                .bindBean(testUserRecord)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertReleaseRecord(Handle handle, String releaseNumber, long systemId) {
        var testReleaseRecord = Release.builder()
                .releaseNumber(releaseNumber)
                .deployableSystemId(systemId)
                .build();

        return handle.createUpdate("insert into manual_deployment_task_releases (release_number, deployable_system_id) values (:releaseNumber, :deployableSystemId)")
                .bindBean(testReleaseRecord)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertReleaseStatusRecord(Handle handle, DeploymentTaskStatus status) {
        var systemId = insertDeployableSystem(handle, "kiwi");
        var releaseId = insertReleaseRecord(handle, "42", systemId);
        return insertReleaseStatusRecord(handle, status, releaseId, systemId);
    }

    public static long insertReleaseStatusRecord(Handle handle, DeploymentTaskStatus status, long releaseId, long systemId) {
        var envId = insertDeploymentEnvironmentRecord(handle, "DEV", systemId);

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
        var systemId = insertDeployableSystem(handle, "kiwi");
        var releaseId = insertReleaseRecord(handle, "42", systemId);
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

    public static long insertTaskStatusRecord(Handle handle, DeploymentTaskStatus status, long taskId, long systemId) {
        var envId = insertDeploymentEnvironmentRecord(handle, "DEV", systemId);

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

    public static long insertBuildRecord(Handle handle, String identifier, String version, long systemId) {
        var buildToInsert = Build.builder()
                .repoNamespace("kiwiproject")
                .repoName(identifier)
                .commitRef("abc1234")
                .commitUser("jdoe")
                .sourceBranch("main")
                .componentIdentifier(identifier)
                .componentVersion(version)
                .distributionLocation("https://some-nexus-server.net/foo")
                .extraDeploymentInfo(Map.of())
                .gitProvider(GitProvider.GITHUB)
                .deployableSystemId(systemId)
                .build();

        return handle.createUpdate("insert into builds "
                        + "(repo_namespace, repo_name, commit_ref, commit_user, source_branch, component_identifier, component_version, distribution_location, extra_deployment_info, git_provider, deployable_system_id) "
                        + "values "
                        + "(:repoNamespace, :repoName, :commitRef, :commitUser, :sourceBranch, :componentIdentifier, :componentVersion, :distributionLocation, :extraData, :gitProvider, :deployableSystemId)")
                .bindBean(buildToInsert)
                .bind("extraData", "{}")
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertHostRecord(Handle handle, String hostname, long envId, long systemId) {
        var hostToInsert = Host.builder()
                .environmentId(envId)
                .hostname(hostname)
                .source(Host.Source.CHAMPAGNE)
                .tags(List.of("foo"))
                .deployableSystemId(systemId)
                .build();

        return handle.createUpdate("insert into hosts "
                        + "(environment_id, hostname, source, tags, deployable_system_id) "
                        + "values "
                        + "(:environmentId, :hostname, :source, :tagCsv, :deployableSystemId)")
                .bindBean(hostToInsert)
                .bind("tagCsv", StringUtils.join(hostToInsert.getTags(), ","))
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertComponentRecord(Handle handle, String componentName, String tag, Long deployableSystemId) {
        var componentToInsert = Component.builder()
                .componentName(componentName)
                .tag(tag)
                .deployableSystemId(deployableSystemId)
                .build();

        return handle.createUpdate("insert into components "
                        + "(component_name, tag, deployable_system_id) "
                        + "values "
                        + "(:componentName, :tag, :deployableSystemId)")
                .bindBean(componentToInsert)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static long insertDeployableSystem(Handle handle, String name) {
        var deployableSystemToInsert = DeployableSystem.builder()
                .name(name)
                .build();

        return handle.createUpdate("insert into deployable_systems "
                        + "(name) "
                        + "values "
                        + "(:name)")
                .bindBean(deployableSystemToInsert)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .first();
    }

    public static void insertUserToDeployableSystemLink(Handle handle, long userId, long deployableSystemId, boolean admin) {
        handle.createUpdate("insert into users_deployable_systems (user_id, deployable_system_id, system_admin) values (:userId, :deployableSystemId, :admin)")
                .bind("userId", userId)
                .bind("deployableSystemId", deployableSystemId)
                .bind("admin", admin)
                .execute();
    }

}
