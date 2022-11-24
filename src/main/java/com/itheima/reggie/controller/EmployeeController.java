package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

import static org.apache.commons.lang.StringUtils.isNotEmpty;



@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    /**
     * 员工登录
     * @param request:登录成功之后需要将员工对象的id存到Session表示登录成功，可以随时获取当前登录的用户
     * @param employee:具体员工
     */
    @PostMapping("/login")
    public R<Employee>login(HttpServletRequest request,@RequestBody Employee employee){
        /**
         * 1.将页面提交的密码password进行md5加密
         * 2.将页面提交的用户名username查询数据库
         * 3.如果没有查询到则返回登录失败结果
         * 4.密码比对，如果不一致则返回登录失败结果
         * 5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
         * 6.登录成功将员工id存入session并返回登录成功结果
         */

        String password=employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(queryWrapper);

        if(emp==null){
            return R.error("登录失败");
        }

        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }

        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     */
    @PostMapping("/logout")
    public R<String>logout(HttpServletRequest request){
        //清理session中保存的当前登录id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     */
    @PostMapping
    public R<String>save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //设置初始密码，进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置创建时间，与更新时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
//        Long empId=(Long)request.getSession().getAttribute("employee");
        //设置id
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
//        try {
//            employeeService.save(employee);
//        }catch (Exception e){
//            R.error("新增员工失败");
//        }
        //使用全局异常捕获
        employeeService.save(employee);
        return R.success("员工新增成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<Employee>();
        //添加过滤条件
        //like相似查询
        queryWrapper.like(isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     */
    @PutMapping
    public R<String>update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        long id=Thread.currentThread().getId();
        log.info("线程id为{}",id);
//        Long empId=(Long)request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     */
    @GetMapping("/{id}")
    public R<Employee>getById(@PathVariable Long id){
        log.info("根据Id查询员工信息...");
        Employee employee=employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }
}