package com.example.myblog.mapper;

import com.example.myblog.domain.Folder;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FolderMapperTest {

    @Autowired
    private FolderMapper folderMapper;

    @Test
    void deletesEmptyFolderById() {
        Folder folder = new Folder();
        folder.setOwnerUserId(1L);
        folder.setName("Disposable");
        folder.setDisplayOrder(99);
        folder.setCreatedAt(LocalDateTime.of(2026, 4, 25, 12, 0));
        folder.setUpdatedAt(folder.getCreatedAt());
        folderMapper.insert(folder);

        int affectedRows = folderMapper.deleteById(folder.getId());

        assertThat(affectedRows).isEqualTo(1);
        assertThat(folderMapper.findById(folder.getId())).isEmpty();
    }

    @Test
    void updatesFolderNameById() {
        Folder folder = folderMapper.findById(10L).orElseThrow();
        folder.setName("Java Advanced");
        folder.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 13, 0));

        int affectedRows = folderMapper.update(folder);

        assertThat(affectedRows).isEqualTo(1);
        assertThat(folderMapper.findById(10L)).get()
                .extracting(Folder::getName)
                .isEqualTo("Java Advanced");
    }
}
