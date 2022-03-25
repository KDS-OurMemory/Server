package com.kds.ourmemory.v1.controller.memory;

import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryReqDto;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryRspDto;
import com.kds.ourmemory.v1.service.memory.MemoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

import static com.kds.ourmemory.v1.controller.ApiResult.ok;

@Api(tags = {"4. Memory"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/memories")
public class MemoryController {
    private final MemoryService memoryService;

    @ApiOperation(value = "일정 추가", notes = "일정을 개인방에 추가한 뒤 추가할 방에 공유한다. 추가/공유 둘다 동일한 기능으로 일정-방 관계데이터를 만들어준다.")
    @PostMapping
    public ApiResult<MemoryRspDto> insert(@RequestBody MemoryReqDto reqDto) {
        return ok(memoryService.insert(reqDto));
    }

    @ApiOperation(value = "일정 개별 조회", notes = "하나의 일정이 여러 방에 포함될 수 있기 때문에 어떤 방에서 일정을 조회하는지 확인하기 위해 방 번호를 받음.")
    @GetMapping("/{memoryId}/room/{roomId}")
    public ApiResult<MemoryRspDto> findMemory(
            @PathVariable long memoryId,
            @PathVariable long roomId
    ) {
        return ok(memoryService.find(memoryId, roomId));
    }

    @ApiOperation(value = "일정 목록 조회", notes = """
            아래 조건에 맞는 일정 목록을 검색한다.
            1. 검색기간 - 전달 시, 사이 값으로 조회
                1) 시작월(yyyy-MM)
                2) 종료월(yyyy-MM)
            2. 검색조건 - 각 조건 별 OR 검색, Equals 검색
                1) 일정 작성자 번호
                2) 일정 제목
            3. 정렬조건 - 오름차순
                1) 일정 시작시간
                2) 일정 생성시간""")
    @GetMapping
    public ApiResult<List<MemoryRspDto>> findMemories(
            @ApiParam(value = "일정 작성자 번호") @RequestParam(required = false) Long writerId,
            @ApiParam(value = "일정 제목") @RequestParam(required = false) String name,
            @ApiParam(value = "일정 시작월", example = "yyyy-MM") @RequestParam(required = false) YearMonth startMonth,
            @ApiParam(value = "일정 종료월", example = "yyyy-MM") @RequestParam(required = false) YearMonth endMonth
    ) {
        return ok(memoryService.findMemories(writerId, name, startMonth, endMonth));
    }

    @ApiOperation(value = "일정 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{memoryId}/writer/{userId}")
    public ApiResult<MemoryRspDto> update(
            @PathVariable long memoryId,
            @PathVariable long userId,
            @RequestBody MemoryReqDto reqDto
    ) {
        return ok(memoryService.update(memoryId, userId, reqDto));
    }

    @ApiOperation(value = "일정 참석 여부 설정", notes = "일정에 참석/불참 여부 설정, 사용자-일정 관계 테이블에 레코드를 추가하는 방식으로 설정함.")
    @PostMapping("/{memoryId}/attendance")
    public ApiResult<MemoryRspDto> setAttend(@PathVariable long memoryId, @RequestBody MemoryReqDto reqDto) {
        return ok(memoryService.setAttendanceStatus(memoryId, reqDto));
    }

    @ApiOperation(value = "일정 공유",
            notes = """
                    ""                    사용자가 대상 목록에게 일정을 공유시킨다.\s
                                        1. 사용자 개별 공유: 각 사용자 별로 방 생성 뒤 일정 공유(type=USERS, targetIds=사용자 번호 목록)\s
                                        2. 사용자 그룹 공유: 사용자들을 참여자로 방 생성 후 일정 공유(type=USER_GROUP, targetIds=사용자 번호 목록)\s
                                        3. 기존 방에 공유: 전달받은 각각의 방에 일정 공유(type=ROOMS, targetIds=방 번호 목록)"""
    )
    @PostMapping("/{memoryId}/share/{userId}")
    public ApiResult<MemoryRspDto> shareMemory(
            @PathVariable long memoryId,
            @PathVariable long userId,
            @RequestBody MemoryReqDto reqDto
    ) {
        return ok(memoryService.shareMemory(memoryId, userId, reqDto));
    }

    @ApiOperation(value = "일정 삭제", notes = """
            일정을 방에서 삭제처리한다. 아래의 경우에 따라 처리 방식이 나뉜다.\s
            1. 공유방에서 삭제 -> 일정-방 관계 삭제\s
            2. 개인방에서 삭제 -> 일정 삭제 처리\s
            성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{memoryId}/users/{userId}/rooms/{roomId}")
    public ApiResult<MemoryRspDto> delete(
            @ApiParam(value = "일정 번호") @PathVariable long memoryId,
            @ApiParam(value = "일정을 삭제하려는 사용자 번호(개인방에서 삭제하는지 확인하기 위해 필요함.)") @PathVariable long userId,
            @ApiParam(value = "일정이 삭제될 방 번호") @PathVariable long roomId) {
        return ok(memoryService.delete(memoryId, userId, roomId));
    }
}
