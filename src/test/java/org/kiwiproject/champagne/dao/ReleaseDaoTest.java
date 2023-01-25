package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.champagne.dao.mappers.ReleaseMapper;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("ReleaseDao")
@ExtendWith(SoftAssertionsExtension.class)
class ReleaseDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<ReleaseDao> daoExtension = Jdbi3DaoExtension.<ReleaseDao>builder()
            .daoType(ReleaseDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private ReleaseDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertRelease {

        @Test
        void shouldInsertReleaseSuccessfully(SoftAssertions softly) {
            var beforeInsert = ZonedDateTime.now();

            var releaseToInsert = Release.builder()
                .releaseNumber("2023.42.0")
                .build();

            var id = dao.insertRelease(releaseToInsert);

            var releases = handle.select("select * from manual_deployment_task_releases where id = ?", id)
                .map(new ReleaseMapper())
                .list();

            assertThat(releases).hasSize(1);

            var release = first(releases);
            softly.assertThat(release.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, release.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, release.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(release.getReleaseNumber()).isEqualTo("2023.42.0");
        }
    }

    @Nested
    class FindPagedReleases {

        @Test
        void shouldReturnListOfReleases() {
            saveTestReleaseRecord("42");

            var releases = dao.findPagedReleases(0, 10);
            assertThat(releases)
                .extracting("releaseNumber")
                .contains("42");
        }

        @Test
        void shouldReturnEmptyListWhenNoReleasesFound() {
            saveTestReleaseRecord("42");

            var releases = dao.findPagedReleases(10, 10);
            assertThat(releases).isEmpty();
        }
    }

    @Nested
    class CountReleases {

        @Test
        void shouldReturnCountOfReleases() {
            saveTestReleaseRecord("42");

            var releases = dao.countReleases();
            assertThat(releases).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoReleasesFound() {
            var releases = dao.countReleases();
            assertThat(releases).isZero();
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteReleaseSuccessfully() {
            var id = saveTestReleaseRecord("42");

            dao.deleteById(id);

            var releases = handle.select("select * from manual_deployment_task_releases where id = ?", id)
                .map(new ReleaseMapper())
                .list();

            assertThat(releases).isEmpty();
        }

    }

    private long saveTestReleaseRecord(String releaseNumber) {
        handle.execute("insert into manual_deployment_task_releases (release_number) values (?)", releaseNumber);

        return handle.select("select * from manual_deployment_task_releases where release_number = ?", releaseNumber)
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();
    }

}
