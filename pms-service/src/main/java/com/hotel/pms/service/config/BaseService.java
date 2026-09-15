package com.hotel.pms.service.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.result.PageRequest;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.dao.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Service基类
 * <p>
 * 提供通用的CRUD操作，封装事务管理
 * </p>
 * 
 * @param <T> 实体类型
 * @param <M> Mapper类型
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
public abstract class BaseService<T extends BaseEntity, M extends BaseMapper<T>> {
    
    @Autowired
    protected M mapper;
    
    /**
     * 根据ID查询
     * 
     * @param id 主键ID
     * @return 实体对象
     */
    public T getById(Serializable id) {
        return mapper.selectById(id);
    }
    
    /**
     * 查询列表
     * 
     * @return 实体列表
     */
    public List<T> list() {
        return mapper.selectList(null);
    }
    
    /**
     * 分页查询
     * 
     * @param pageRequest 分页请求
     * @return 分页响应
     */
    public PageResponse<T> page(PageRequest pageRequest) {
        // 【构建分页对象】
        Page<T> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        
        // 【执行分页查询】
        IPage<T> result = mapper.selectPage(page, null);
        
        // 【返回分页响应】
        return new PageResponse<>(result.getRecords(), result.getTotal(), 
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 新增（带事务）
     * 
     * @param entity 实体对象
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int save(T entity) {
        return mapper.insert(entity);
    }
    
    /**
     * 批量新增（带事务）
     * 
     * @param entityList 实体列表
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveBatch(Collection<T> entityList) {
        int count = 0;
        for (T entity : entityList) {
            count += mapper.insert(entity);
        }
        return count;
    }
    
    /**
     * 更新（带事务）
     * 
     * @param entity 实体对象
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(T entity) {
        return mapper.updateById(entity);
    }
    
    /**
     * 删除（逻辑删除，带事务）
     * 
     * @param id 主键ID
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Serializable id) {
        return mapper.deleteById(id);
    }
    
    /**
     * 批量删除（逻辑删除，带事务）
     * 
     * @param ids 主键ID列表
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatch(Collection<? extends Serializable> ids) {
        return mapper.deleteBatchIds(ids);
    }
    
    /**
     * 统计数量
     * 
     * @return 数量
     */
    public long count() {
        return mapper.selectCount(null);
    }
    
    /**
     * 判断是否存在
     * 
     * @param id 主键ID
     * @return 是否存在
     */
    public boolean exists(Serializable id) {
        return mapper.selectById(id) != null;
    }
}