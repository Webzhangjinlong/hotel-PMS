package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.StayGuestDTO;
import com.hotel.pms.common.dto.StayGuestVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.stay.StayGuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入住同住人控制器
 * <p>
 * 处理入住同住人的增删改查请求
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/stay-guests")
@RequiredArgsConstructor
@Tag(name = "入住同住人管理", description = "入住同住人的增删查")
public class StayGuestController {

    private final StayGuestService stayGuestService;

    /**
     * 查询入住单的所有同住人
     *
     * @param stayId 入住单ID
     * @return 同住人列表（含主客人）
     */
    @GetMapping("/stay/{stayId}")
    @Operation(summary = "查询同住人列表", description = "根据入住单ID查询所有同住人")
    public Result<List<StayGuestVO>> getByStayId(
            @Parameter(description = "入住单ID") @PathVariable Long stayId) {
        List<StayGuestVO> result = stayGuestService.getByStayId(stayId);
        return Result.success(result);
    }

    /**
     * 添加同住人
     *
     * @param stayId 入住单ID
     * @param hotelId 酒店ID
     * @param dto    同住人信息
     * @return 添加的同住人
     */
    @PostMapping("/stay/{stayId}")
    @Operation(summary = "添加同住人", description = "为入住单添加同住人")
    public Result<StayGuestVO> addCoGuest(
            @Parameter(description = "入住单ID") @PathVariable Long stayId,
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Valid @RequestBody StayGuestDTO dto) {
        StayGuestVO result = stayGuestService.addCoGuest(stayId, hotelId, dto);
        return Result.success(result);
    }

    /**
     * 删除同住人
     *
     * @param id 同住人ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除同住人", description = "删除指定的同住人（主客人不能删除）")
    public Result<Void> removeCoGuest(
            @Parameter(description = "同住人ID") @PathVariable Long id) {
        stayGuestService.removeCoGuest(id);
        return Result.success();
    }

    /**
     * 查询入住单的客人姓名列表
     * <p>
     * 用于公安上传等场景
     * </p>
     *
     * @param stayId 入住单ID
     * @return 客人姓名列表
     */
    @GetMapping("/stay/{stayId}/names")
    @Operation(summary = "查询客人姓名列表", description = "查询入住单的所有客人姓名")
    public Result<List<String>> getGuestNames(
            @Parameter(description = "入住单ID") @PathVariable Long stayId) {
        List<String> result = stayGuestService.getGuestNames(stayId);
        return Result.success(result);
    }
}
