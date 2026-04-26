package com.example.myblog.domain.view;

import java.util.List;

public record HomePageView(String ownerAccount, boolean isViewerOwner, List<FolderView> folders) {
}

