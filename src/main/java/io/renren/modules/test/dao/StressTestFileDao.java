package io.renren.modules.test.dao;

import io.renren.modules.sys.dao.BaseDao;
import io.renren.modules.test.entity.StressTestFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface StressTestFileDao extends BaseDao<StressTestFileEntity> {

    int deleteBatchByCaseIds(Object[] id);

    List<StressTestFileEntity> queryListForDelete(Map<String, Object> map);

    StressTestFileEntity queryObjectForClone(Map<String, Object> map);

    int updateStatusBatch(Map<String, Object> map);

}
