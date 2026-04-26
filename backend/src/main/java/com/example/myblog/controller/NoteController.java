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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteSavedView createNote(@Valid @RequestBody CreateNoteRequest request, Principal principal) {
        return noteCommandService.create(principal.getName(), request);
    }

    @PutMapping("/{noteId}")
    public NoteSavedView updateNote(@PathVariable Long noteId,
                                    @Valid @RequestBody UpdateNoteRequest request,
                                    Principal principal) {
        return noteCommandService.update(principal.getName(), noteId, request);
    }
}

