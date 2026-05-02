package com.example.myblog.unit;

import com.example.myblog.domain.Folder;
import com.example.myblog.domain.UserAccount;
import com.example.myblog.domain.exception.ConflictException;
import com.example.myblog.mapper.FolderMapper;
import com.example.myblog.mapper.NoteMapper;
import com.example.myblog.mapper.UserAccountMapper;
import com.example.myblog.service.FolderService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderMapper folderMapper;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Test
    void creatingFolderRejectsDuplicateNameIgnoringCase() {
        FolderService service = new FolderService(folderMapper, noteMapper, userAccountMapper);
        UserAccount user = user(1L, "alice");
        Folder existingFolder = folder(10L, 1L, "Java Basics");
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(folderMapper.findByOwnerUserIdAndName(1L, "java basics")).thenReturn(Optional.of(existingFolder));

        assertThatThrownBy(() -> service.createFolder("alice", "  java basics  "))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Folder name already exists");

        verify(folderMapper, never()).insert(org.mockito.ArgumentMatchers.any(Folder.class));
    }

    @Test
    void renamingFolderAllowsKeepingSameNameWithDifferentCase() {
        FolderService service = new FolderService(folderMapper, noteMapper, userAccountMapper);
        UserAccount user = user(1L, "alice");
        Folder existingFolder = folder(10L, 1L, "Java Basics");
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(folderMapper.findById(10L)).thenReturn(Optional.of(existingFolder));
        when(folderMapper.findByOwnerUserIdAndName(1L, "java basics")).thenReturn(Optional.of(existingFolder));

        service.renameFolder("alice", 10L, "  java basics  ");

        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folderMapper).update(folderCaptor.capture());
        assertThat(folderCaptor.getValue().getName()).isEqualTo("java basics");
    }

    @Test
    void renamingFolderRejectsAnotherFoldersName() {
        FolderService service = new FolderService(folderMapper, noteMapper, userAccountMapper);
        UserAccount user = user(1L, "alice");
        Folder targetFolder = folder(10L, 1L, "Java Basics");
        Folder existingFolder = folder(11L, 1L, "Recipes");
        when(userAccountMapper.findByAccount("alice")).thenReturn(Optional.of(user));
        when(folderMapper.findById(10L)).thenReturn(Optional.of(targetFolder));
        when(folderMapper.findByOwnerUserIdAndName(1L, "Recipes")).thenReturn(Optional.of(existingFolder));

        assertThatThrownBy(() -> service.renameFolder("alice", 10L, "Recipes"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Folder name already exists");

        verify(folderMapper, never()).update(org.mockito.ArgumentMatchers.any(Folder.class));
    }

    private static UserAccount user(Long id, String account) {
        UserAccount user = new UserAccount();
        user.setId(id);
        user.setAccount(account);
        return user;
    }

    private static Folder folder(Long id, Long ownerUserId, String name) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setOwnerUserId(ownerUserId);
        folder.setName(name);
        return folder;
    }
}
