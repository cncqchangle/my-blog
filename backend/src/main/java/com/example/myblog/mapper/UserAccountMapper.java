package com.example.myblog.mapper;

import com.example.myblog.domain.UserAccount;
import com.example.myblog.domain.view.UserSearchResultView;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    Optional<UserAccount> findByAccount(@Param("account") String account);

    Optional<UserAccount> findById(@Param("id") Long id);

    int insert(UserAccount userAccount);

    List<UserSearchResultView> searchByAccount(@Param("query") String query,
                                               @Param("excludeAccount") String excludeAccount,
                                               @Param("limit") int limit);
}
