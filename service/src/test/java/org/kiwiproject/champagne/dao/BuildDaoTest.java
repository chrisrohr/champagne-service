package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertBuildRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.constants.KiwiTestConstants.JSON_HELPER;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kiwiproject.champagne.dao.mappers.BuildMapper;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.champagne.model.GitProvider;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

@DisplayName("BuildDao")
class BuildDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<BuildDao> daoExtension = Jdbi3DaoExtension.<BuildDao>builder()
            .daoType(BuildDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .plugin(new JdbiPlugin() {
                
                @Override
                public void customizeJdbi(Jdbi jdbi) {
                    jdbi.registerRowMapper(Build.class, new BuildMapper(JSON_HELPER));
                }
            })
            .build();

    private BuildDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertBuild {

        @Test
        void shouldInsertBuildSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var buildToInsert = Build.builder()
                .repoNamespace("kiwiproject")
                .repoName("champagne-service")
                .commitRef("abc1234")
                .commitUser("jdoe")
                .sourceBranch("main")
                .componentIdentifier("champagne_service")
                .componentVersion("42.0.0")
                .distributionLocation("https://some-nexus-server.net/foo")
                .extraDeploymentInfo(Map.of())
                .gitProvider(GitProvider.GITHUB)
                .build();

            var id = dao.insertBuild(buildToInsert, "{}");

            var builds = handle.select("select * from builds where id = ?", id)
                .map(new BuildMapper(JSON_HELPER))
                .list();

            assertThat(builds).hasSize(1);

            var build = first(builds);
            assertThat(build.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, build.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(build).usingRecursiveComparison().ignoringFields("id", "createdAt").isEqualTo(buildToInsert);
        }
    }

    @Nested
    class FindPagedBuilds {

        @ParameterizedTest
        @CsvSource(nullValues = "null", value = {
            "null, null",
            "null, 42.0",
            "champagne-service, null",
            "champagne-service, 42.0"
        })
        void shouldReturnListOfBuilds(String componentIdentifierFilter, String componentVersionFilter) {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertBuildRecord(handle, "champagne-service", "42.0", systemId);

            var builds = dao.findPagedBuilds(0, 10, systemId, componentIdentifierFilter, componentVersionFilter);
            assertThat(builds)
                .extracting("componentIdentifier", "componentVersion")
                .contains(tuple("champagne-service", "42.0"));
        }

        @Test
        void shouldReturnEmptyListWhenNoBuildsFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertBuildRecord(handle, "champagne-service", "42.0", systemId);

            var builds = dao.findPagedBuilds(10, 10, systemId, null, null);
            assertThat(builds).isEmpty();
        }
    }

    @Nested
    class CountBuilds {

        @ParameterizedTest
        @CsvSource(nullValues = "null", value = {
            "null, null",
            "null, 42.0",
            "champagne-service, null",
            "champagne-service, 42.0"
        })
        void shouldReturnCountOfBuilds(String componentIdentifierFilter, String componentVersionFilter) {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertBuildRecord(handle, "champagne-service", "42.0", systemId);

            var builds = dao.countBuilds(systemId, componentIdentifierFilter, componentVersionFilter);
            assertThat(builds).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoBuildsFound() {
            var builds = dao.countBuilds(1L, null, null);
            assertThat(builds).isZero();
        }
    }
   
}
