package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertComponentRecord;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.model.Component;
import org.kiwiproject.champagne.dao.mappers.ComponentMapper;
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

            var componentToInsert = Component.builder()
                .componentName("foo-service")
                .tag("core")
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
            var id = insertComponentRecord(handle, "foo-service", "core");

            var components = dao.findComponentsByHostTags(List.of("core"));
            assertThat(components)
                .extracting("id", "componentName", "tag")
                .contains(tuple(id, "foo-service", "core"));
        }

        @Test
        void shouldReturnEmptyListWhenNoComponentsFound() {
            insertComponentRecord(handle, "foo-service", "audit");

            var hosts = dao.findComponentsByHostTags(List.of("core"));
            assertThat(hosts).isEmpty();
        }
    }

    @Nested
    class DeleteComponent {

        @Test
        void shouldDeleteComponentSuccessfully() {
            var id = insertComponentRecord(handle, "foo-service", "core");

            dao.deleteComponent(id);

            var components = handle.select("select * from components where id = ?", id).map(new ComponentMapper()).list();
            assertThat(components).isEmpty();
        }

    }

}
