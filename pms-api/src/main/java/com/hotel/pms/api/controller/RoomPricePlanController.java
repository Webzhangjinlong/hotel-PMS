package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.RoomPricePlanCreateDTO;
import com.hotel.pms.common.dto.RoomPricePlanUpdateDTO;
import com.hotel.pms.common.dto.RoomPricePlanVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.price.RoomPricePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/price-plans")
@RequiredArgsConstructor
public class RoomPricePlanController {

    private final RoomPricePlanService roomPricePlanService;

    @GetMapping
    public Result<PageResponse<RoomPricePlanVO>> pageList(
            @RequestParam Long hotelId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(roomPricePlanService.pageList(hotelId, name, page, size));
    }

    @GetMapping("/list")
    public Result<List<RoomPricePlanVO>> list(@RequestParam Long hotelId) {
        return Result.success(roomPricePlanService.listByHotelId(hotelId));
    }

    @GetMapping("/{id}")
    public Result<RoomPricePlanVO> getById(@PathVariable Long id) {
        return Result.success(roomPricePlanService.getById(id));
    }

    @PostMapping
    public Result<RoomPricePlanVO> create(@Valid @RequestBody RoomPricePlanCreateDTO dto) {
        return Result.success(roomPricePlanService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<RoomPricePlanVO> update(
            @PathVariable Long id,
            @Valid @RequestBody RoomPricePlanUpdateDTO dto) {
        return Result.success(roomPricePlanService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomPricePlanService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        roomPricePlanService.updateStatus(id, status);
        return Result.success();
    }
}