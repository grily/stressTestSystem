package io.renren.modules.test.dao;

import io.renren.modules.sys.dao.BaseDao;
import io.renren.modules.test.entity.DebugTestReportsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface DebugTestReportsDao extends BaseDao<DebugTestReportsEntity> {

    int deleteBatchByCaseIds(Object[] id);
}
