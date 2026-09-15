package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门锁设备配置实体类
 * <p>
 * 对应数据库表：door_lock_config
 * 存储门锁设备的配置信息
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("door_lock_config")
public class DoorLockConfig extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 设备名称 */
    @TableField("device_name")
    private String deviceName;
    
    /** 设备型号 */
    @TableField("device_model")
    private String deviceModel;
    
    /** 设备IP地址 */
    @TableField("device_ip")
    private String deviceIp;
    
    /** 设备端口 */
    @TableField("device_port")
    private String devicePort;
    
    /** 设备状态：OFFLINE-离线/ONLINE-在线/ERROR-故障 */
    @TableField("device_status")
    private String deviceStatus;
    
    /** 配置信息（JSON格式） */
    @TableField("config_json")
    private String configJson;
    
    /** 配置状态：ACTIVE-启用/INACTIVE-停用 */
    @TableField("status")
    private String status;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}
