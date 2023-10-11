package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    public Result emploeeSave(@RequestBody EmployeeDTO employeeDTO){
        log.info("EmployeeController执行结束，结果{emploeeSave}",employeeDTO);
        employeeService.saveEmploee(employeeDTO);
        return Result.success();
    }

    @GetMapping("page")
    public Result<PageResult> getEmployeePage(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("EmployeeController的getEmployeePage方法执行中，参数为{}",employeePageQueryDTO);
        PageResult pageResult=employeeService.getEmployeePage(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping("status/{status}")
    public Result employeeStatus(@PathVariable Integer status,Long id){
        log.info("EmployeeController的employeeStatus方法执行中，参数为{}",status,id);
        employeeService.employeeStatus(id,status);
        return Result.success();
    }
    @GetMapping("{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id){
        log.info("EmployeeController的getEmployeeById方法执行中，参数为{}", id);
        return Result.success(employeeService.getEmployeeById(id));
    }

    @PutMapping
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("EmployeeController的updateEmployee方法执行中，参数为{}",employeeDTO);
        employeeService.updateEmployee(employeeDTO);
        return Result.success();
    }


    @PutMapping("editPassword")  //前端传的参数有问题  没有传employeeId,需要手动获取
    public Result updatePassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("EmployeeController的updatePassword方法执行中，参数为{}",passwordEditDTO);
        employeeService.updatePassword(passwordEditDTO);
        return Result.success();
    }
}
