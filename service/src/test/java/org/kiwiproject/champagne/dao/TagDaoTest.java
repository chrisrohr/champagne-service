package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertTagRecord;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.TagMapper;
import org.kiwiproject.champagne.model.Tag;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("TagDao")
class TagDaoTest {

    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<TagDao> daoExtension = Jdbi3DaoExtension.<TagDao>builder()
            .daoType(TagDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private TagDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertTag {

        @Test
        void shouldInsertTagSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var systemId = insertDeployableSystem(handle, "kiwi");
            var tagToInsert = Tag.builder()
                    .name("core")
                    .deployableSystemId(systemId)
                    .build();

            var id = dao.insertTag(tagToInsert);

            var tags = handle.select("select * from tags where id = ?", id)
                    .map(new TagMapper())
                    .list();

            assertThat(tags).hasSize(1);

            var tag = first(tags);
            assertThat(tag.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, tag.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(tag)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "createdAt", "updatedAt")
                    .isEqualTo(tagToInsert);
        }
    }

    @Nested
    class FindTagsForSystem {

        @Test
        void shouldReturnListOfTags() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var id = insertTagRecord(handle, "core", systemId);

            var tags = dao.findTagsForSystem(systemId);
            assertThat(tags)
                    .extracting("id", "name")
                    .contains(tuple(id, "core"));
        }

        @Test
        void shouldReturnEmptyListWhenNoTagsFound() {
            var systemId = insertDeployableSystem(handle, "my-system");
            insertTagRecord(handle, "audit", systemId);

            var tags = dao.findTagsForSystem(500L);
            assertThat(tags).isEmpty();
        }
    }

    @Nested
    class DeleteTag {

        @Test
        void shouldDeleteTagSuccessfully() {
            var systemId = insertDeployableSystem(handle, "my-system");
            var id = insertTagRecord(handle, "core", systemId);

            dao.deleteTag(id);

            var tags = handle.select("select * from tags where id = ?", id).map(new TagMapper()).list();
            assertThat(tags).isEmpty();
        }

    }

}
