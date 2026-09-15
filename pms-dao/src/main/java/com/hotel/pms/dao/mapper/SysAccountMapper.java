package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.SysAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统账号Mapper接口
 * <p>
 * 负责系统账号的数据访问
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface SysAccountMapper extends BaseMapper<SysAccount> {
    
    /**
     * 根据酒店ID和用户名查询账号
     * 
     * @param hotelId 酒店ID
     * @param username 用户名
     * @return 账号信息
     */
    @Select("SELECT * FROM sys_account WHERE hotel_id = #{hotelId} AND username = #{username} AND deleted = FALSE")
    SysAccount selectByHotelIdAndUsername(@Param("hotelId") Long hotelId, @Param("username") String username);
    
    /**
     * 根据用户名查询账号（全局）
     * 
     * @param username 用户名
     * @return 账号信息
     */
    @Select("SELECT * FROM sys_account WHERE username = #{username} AND deleted = FALSE")
    SysAccount selectByUsername(@Param("username") String username);
}