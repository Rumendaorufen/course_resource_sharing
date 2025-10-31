package com.course.controller;
import com.course.common.api.ApiResult;
import com.course.dto.UserDTO;
import com.course.entity.User;
import com.course.service.UserService;
import com.course.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理接口", description = "用户相关的接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResult<UserVO> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ApiResult.success(userVO);
    }

    @PutMapping("/{id}")
    public ApiResult<Void> updateUser(@PathVariable Long id, @Validated @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return ApiResult.success();
    }

    @PutMapping("/{id}/password")
    public ApiResult<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.updatePassword(id, oldPassword, newPassword);
        return ApiResult.success();
    }

    @GetMapping("/teachers")
    @Operation(summary = "获取所有教师", description = "获取所有角色为TEACHER的用户")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "成功获取教师列表")
    })
    public ApiResult<List<UserVO>> getAllTeachers() {
        List<User> teachers = userService.getAllTeachers();
        List<UserVO> teacherVOs = teachers.stream()
            .map(teacher -> {
                UserVO vo = new UserVO();
                BeanUtils.copyProperties(teacher, vo);
                return vo;
            })
            .collect(Collectors.toList());
        return ApiResult.success(teacherVOs);
    }
}
