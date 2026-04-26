package com.example.myblog.domain.view;

import java.util.List;

public record FolderView(Long folderId, String folderName, int noteCount, List<NoteSummaryView> notes) {
}

