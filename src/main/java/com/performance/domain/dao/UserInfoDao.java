package com.performance.domain.dao;

import java.util.List;

import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;

import com.performance.domain.entity.UserInfo;

@ConfigAutowireable
@Dao
public interface UserInfoDao {

    @Insert
    public int Insert(UserInfo entity);
    
    @Select
    public List<UserInfo> search(UserInfo entity, List<String> hobbies);
}
