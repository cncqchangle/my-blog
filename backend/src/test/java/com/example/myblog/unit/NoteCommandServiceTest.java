package com.example.myblog.unit;

import com.example.myblog.domain.Folder;
import com.example.myblog.domain.Note;
import com.example.myblog.domain.UserAccount;
import com.example.myblog.domain.command.CreateNoteRequest;
import com.example.myblog.domain.command.UpdateNoteRequest;
import com.example.myblog.mapper.FolderMapper;
import com.example.myblog.mapper.NoteMapper;
import com.example.myblog.mapper.UserAccountMapper;
import com.example.myblog.service.CoverImageStorageService;
import com.example.myblog.service.MarkdownRenderService;
import com.example.myblog.service.NoteCommandService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteCommandServiceTest {

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private FolderMapper folderMapper;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private CoverImageStorageService coverImageStorageService;

    private final MarkdownRenderService markdownRenderService = new MarkdownRenderService();

    @Test
    void creatingNoteWithoutUploadGeneratesCoverFromTitle() {
        NoteCommandService service = new NoteCommandService(
                userAccountMapper,
                folderMapper,
                noteMapper,
                markdownRenderService,
                coverImageStorageService);
        UserAccount user = user(1L, "alice");
        Folder folder = folder(10L, 1L);
        CreateNoteRequest request = new CreateNoteRequest();
        request.setTitle("Streams");
        request.setMarkdownContent("# Streams");
        request.setFolderId(10L);
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(folderMapper.findById(10L)).thenReturn(Optional.of(folder));
        when(coverImageStorageService.generateCoverImage("alice", "Streams"))
                .thenReturn("https://test-bucket.example.com/my-blog/generated-note-covers/alice/demo.svg");

        service.create("alice", request);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteMapper).insert(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getCoverImageUrl())
                .isEqualTo("https://test-bucket.example.com/my-blog/generated-note-covers/alice/demo.svg");
        verify(coverImageStorageService).generateCoverImage("alice", "Streams");
    }

    @Test
    void updatingGeneratedCoverWithoutUploadRegeneratesFromNewTitle() {
        NoteCommandService service = new NoteCommandService(
                userAccountMapper,
                folderMapper,
                noteMapper,
                markdownRenderService,
                coverImageStorageService);
        UserAccount user = user(1L, "alice");
        Folder folder = folder(10L, 1L);
        Note note = note(100L, 1L, "https://test-bucket.example.com/my-blog/generated-note-covers/alice/original.svg");
        note.setFolderId(10L);
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setTitle("Streams Deep Dive");
        request.setMarkdownContent("# Streams Deep Dive");
        request.setFolderId(10L);
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(folderMapper.findById(10L)).thenReturn(Optional.of(folder));
        when(noteMapper.findById(100L)).thenReturn(Optional.of(note));
        when(coverImageStorageService.isGeneratedCoverImageUrl(note.getCoverImageUrl())).thenReturn(true);
        when(coverImageStorageService.generateCoverImage("alice", "Streams Deep Dive"))
                .thenReturn("https://test-bucket.example.com/my-blog/generated-note-covers/alice/updated.svg");

        service.update("alice", 100L, request);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteMapper).update(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getCoverImageUrl())
                .isEqualTo("https://test-bucket.example.com/my-blog/generated-note-covers/alice/updated.svg");
        verify(coverImageStorageService).deleteManagedCoverImage("https://test-bucket.example.com/my-blog/generated-note-covers/alice/original.svg");
    }

    @Test
    void updatingCustomCoverWithoutUploadKeepsExistingImage() {
        NoteCommandService service = new NoteCommandService(
                userAccountMapper,
                folderMapper,
                noteMapper,
                markdownRenderService,
                coverImageStorageService);
        UserAccount user = user(1L, "alice");
        Folder folder = folder(11L, 1L);
        Note note = note(101L, 1L, "https://static.example.com/covers/soup.png");
        note.setFolderId(11L);
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setTitle("Soup Updated");
        request.setMarkdownContent("Warm and cozy");
        request.setFolderId(11L);
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(folderMapper.findById(11L)).thenReturn(Optional.of(folder));
        when(noteMapper.findById(101L)).thenReturn(Optional.of(note));
        when(coverImageStorageService.isGeneratedCoverImageUrl(note.getCoverImageUrl())).thenReturn(false);
        when(coverImageStorageService.resolveCoverImageUrl(note.getCoverImageUrl()))
                .thenReturn("https://static.example.com/covers/soup.png");

        service.update("alice", 101L, request);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteMapper).update(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getCoverImageUrl()).isEqualTo("https://static.example.com/covers/soup.png");
        verify(coverImageStorageService, never()).generateCoverImage("alice", "Soup Updated");
        verify(coverImageStorageService, never()).deleteManagedCoverImage("https://static.example.com/covers/soup.png");
    }

    @Test
    void deletingNoteSwallowsManagedCoverCleanupFailures() {
        NoteCommandService service = new NoteCommandService(
                userAccountMapper,
                folderMapper,
                noteMapper,
                markdownRenderService,
                coverImageStorageService);
        UserAccount user = user(1L, "alice");
        Note note = note(100L, 1L, "https://managed.example.com/my-blog/note-covers/alice/demo.png");
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(noteMapper.findById(100L)).thenReturn(Optional.of(note));
        doThrow(new IllegalStateException("oss unavailable"))
                .when(coverImageStorageService).deleteManagedCoverImage(note.getCoverImageUrl());

        assertThatCode(() -> service.delete("alice", 100L)).doesNotThrowAnyException();

        verify(noteMapper).deleteById(100L);
        verify(coverImageStorageService).deleteManagedCoverImage(note.getCoverImageUrl());
    }

    @Test
    void deletingNoteWithoutCoverStillDeletesDatabaseRecord() {
        NoteCommandService service = new NoteCommandService(
                userAccountMapper,
                folderMapper,
                noteMapper,
                markdownRenderService,
                coverImageStorageService);
        UserAccount user = user(1L, "alice");
        Note note = note(100L, 1L, null);
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(noteMapper.findById(100L)).thenReturn(Optional.of(note));

        assertThatCode(() -> service.delete("alice", 100L)).doesNotThrowAnyException();

        verify(noteMapper).deleteById(100L);
        verify(coverImageStorageService).deleteManagedCoverImage(null);
    }

    private static UserAccount user(Long id, String account) {
        UserAccount user = new UserAccount();
        user.setId(id);
        user.setAccount(account);
        return user;
    }

    private static Folder folder(Long id, Long ownerUserId) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setOwnerUserId(ownerUserId);
        return folder;
    }

    private static Note note(Long id, Long authorUserId, String coverImageUrl) {
        Note note = new Note();
        note.setId(id);
        note.setAuthorUserId(authorUserId);
        note.setCoverImageUrl(coverImageUrl);
        return note;
    }
}
