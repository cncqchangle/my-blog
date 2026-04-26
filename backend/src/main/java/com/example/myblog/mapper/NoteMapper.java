package com.example.myblog.mapper;

import com.example.myblog.domain.Note;
import com.example.myblog.domain.view.NoteDetailRow;
import com.example.myblog.domain.view.NoteSummaryView;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoteMapper {

    List<NoteSummaryView> findSummariesByFolderId(@Param("folderId") Long folderId);

    Optional<Note> findById(@Param("id") Long id);

    Optional<NoteDetailRow> findDetailById(@Param("noteId") Long noteId);

    int insert(Note note);

    int update(Note note);
}

