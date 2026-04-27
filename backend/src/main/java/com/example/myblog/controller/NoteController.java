package com.example.myblog.controller;

import com.example.myblog.domain.command.CreateNoteRequest;
import com.example.myblog.domain.command.UpdateNoteRequest;
import com.example.myblog.domain.view.NoteDetailView;
import com.example.myblog.domain.view.NoteSavedView;
import com.example.myblog.service.NoteCommandService;
import com.example.myblog.service.NoteQueryService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteQueryService noteQueryService;
    private final NoteCommandService noteCommandService;

    public NoteController(NoteQueryService noteQueryService,
                          NoteCommandService noteCommandService) {
        this.noteQueryService = noteQueryService;
        this.noteCommandService = noteCommandService;
    }

    @GetMapping("/{noteId}")
    public NoteDetailView getNote(@PathVariable Long noteId, Principal principal) {
        return noteQueryService.getNoteDetail(principal.getName(), noteId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NoteSavedView createNote(@Valid @ModelAttribute CreateNoteRequest request, Principal principal) {
        return noteCommandService.create(principal.getName(), request);
    }

    @PutMapping(value = "/{noteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteSavedView updateNote(@PathVariable Long noteId,
                                    @Valid @ModelAttribute UpdateNoteRequest request,
                                    Principal principal) {
        return noteCommandService.update(principal.getName(), noteId, request);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long noteId, Principal principal) {
        noteCommandService.delete(principal.getName(), noteId);
    }
}

