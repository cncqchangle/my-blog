package com.example.myblog.mapper;

import com.example.myblog.domain.Folder;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FolderMapper {

    List<Folder> findByOwnerUserId(@Param("ownerUserId") Long ownerUserId);

    Optional<Folder> findById(@Param("id") Long id);

    Optional<Folder> findByOwnerUserIdAndName(@Param("ownerUserId") Long ownerUserId,
                                              @Param("name") String name);

    Integer nextDisplayOrder(@Param("ownerUserId") Long ownerUserId);

    int insert(Folder folder);

    int update(Folder folder);

    int deleteById(@Param("id") Long id);
}

