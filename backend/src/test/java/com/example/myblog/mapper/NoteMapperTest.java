package com.example.myblog.mapper;

import com.example.myblog.service.MarkdownRenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NoteMapperTest {

    @Autowired
    private NoteMapper noteMapper;

    @Test
    void loadsJoinedNoteDetailRow() {
        var detail = noteMapper.findDetailById(100L).orElseThrow();
        assertThat(detail.authorAccount()).isEqualTo("alice");
        assertThat(detail.folderName()).isEqualTo("Java Basics");
        assertThat(detail.coverImageUrl()).isNull();
    }

    @Test
    void updatesMarkdownFields() {
        var note = noteMapper.findById(100L).orElseThrow();
        note.setMarkdownContent("# Changed");
        note.setCoverImageUrl("https://static.example.com/covers/changed.png");
        note.setRenderedHtml(new MarkdownRenderService().render(note.getMarkdownContent()));
        noteMapper.update(note);

        assertThat(noteMapper.findById(100L).orElseThrow().getRenderedHtml()).contains("<h1>Changed</h1>");
        assertThat(noteMapper.findById(100L).orElseThrow().getCoverImageUrl()).isEqualTo("https://static.example.com/covers/changed.png");
    }

    @Test
    void loadsSummaryRowsWithCoverAndMarkdownContent() {
        var summaries = noteMapper.findSummariesByFolderId(10L);

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().coverImageUrl()).isNull();
        assertThat(summaries.getFirst().markdownContent()).contains("Useful stream notes.");
    }

    @Test
    void countsAndDeletesNotesByFolder() {
        assertThat(noteMapper.countByFolderId(10L)).isEqualTo(1);

        int affectedRows = noteMapper.deleteById(100L);

        assertThat(affectedRows).isEqualTo(1);
        assertThat(noteMapper.findById(100L)).isEmpty();
        assertThat(noteMapper.countByFolderId(10L)).isZero();
    }
}
