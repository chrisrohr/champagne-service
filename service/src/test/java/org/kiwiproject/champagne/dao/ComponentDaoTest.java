package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertComponentRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertTagRecord;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.ComponentMapper;
import org.kiwiproject.champagne.model.Component;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("ComponentDao")
class ComponentDaoTest {

    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<ComponentDao> daoExtension = Jdbi3DaoExtension.<ComponentDao>builder()
            .daoType(ComponentDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private ComponentDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertComponent {

        @Test
        void shouldInsertComponentSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var systemId = insertDeployableSystem(handle, "kiwi");
            var tagId = insertTagRecord(handle, "core", systemId);
            var componentToInsert = Component.builder()
                    .componentName("foo-service")
                    .tagId(tagId)
                    .deployableSystemId(systemId)
                    .build();

            var id = dao.insertComponent(componentToInsert);

            var components = handle.select("select * from components where id = ?", id)
                    .map(new ComponentMapper())
                    .list();

            assertThat(components).hasSize(1);

            var component = first(components);
            assertThat(component.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, component.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, component.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(component)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "createdAt", "updatedAt")
                    .isEqualTo(componentToInsert);
        }
    }

    @Nested
    class FindComponentsByHostTags {

        @Test
        void shouldReturnListOfComponents() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var tagId = insertTagRecord(handle, "core", systemId);
            var id = insertComponentRecord(handle, "foo-service", tagId, systemId);

            var components = dao.findComponentsByHostTags(List.of(tagId));
            assertThat(components)
                    .extracting("id", "componentName", "tagId")
                    .contains(tuple(id, "foo-service", tagId));
        }

        @Test
        void shouldReturnEmptyListWhenNoComponentsFound() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var tagId = insertTagRecord(handle, "audit", systemId);
            insertComponentRecord(handle, "foo-service", tagId, systemId);

            var hosts = dao.findComponentsByHostTags(List.of(10_000L));
            assertThat(hosts).isEmpty();
        }
    }

    @Nested
    class FindComponentsForSystem {

        @Test
        void shouldReturnListOfComponents() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var tagId = insertTagRecord(handle, "core", systemId);
            var id = insertComponentRecord(handle, "foo-service", tagId, systemId);

            var components = dao.findComponentsForSystem(systemId);
            assertThat(components)
                    .extracting("id", "componentName", "tagId")
                    .contains(tuple(id, "foo-service", tagId));
        }

        @Test
        void shouldReturnEmptyListWhenNoComponentsFound() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var tagId = insertTagRecord(handle, "audit", systemId);
            insertComponentRecord(handle, "foo-service", tagId, systemId);

            var hosts = dao.findComponentsForSystem(500L);
            assertThat(hosts).isEmpty();
        }
    }

    @Nested
    class DeleteComponent {

        @Test
        void shouldDeleteComponentSuccessfully() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var tagId = insertTagRecord(handle, "core", systemId);
            var id = insertComponentRecord(handle, "foo-service", tagId, systemId);

            dao.deleteComponent(id);

            var components = handle.select("select * from components where id = ?", id).map(new ComponentMapper()).list();
            assertThat(components).isEmpty();
        }

    }

    @Nested
    class UpdateComponent {
        @Test
        void shouldUpdateSuccessfully() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var badTag = insertTagRecord(handle, "badTag", systemId);
            var goodTag = insertTagRecord(handle, "goodTag", systemId);
            var id = insertComponentRecord(handle, "old-name", badTag, systemId);

            var updateCount = dao.updateComponent("new-name", goodTag, id);

            assertThat(updateCount).isOne();

            var component = handle.select("select * from components where id = ?", id)
                    .map(new ComponentMapper())
                    .first();

            assertThat(component.getComponentName()).isEqualTo("new-name");
            assertThat(component.getTagId()).isEqualTo(goodTag);
        }
    }

}
