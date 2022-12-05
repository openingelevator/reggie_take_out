package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import static org.apache.logging.log4j.util.Strings.isNotEmpty;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private  CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     */
    @PostMapping
    public R<String>save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页构造器
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto>setmealDtoPage=new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Setmeal>queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(isNotEmpty(name),Setmeal::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> records=pageInfo.getRecords();

        List<SetmealDto>list=records.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            //拷贝相关属性
            BeanUtils.copyProperties(item,setmealDto);

            //查询分类名称
            Long id=item.getCategoryId();
            Category category =categoryService.getById(id);
            if(category!=null){
                String categoryName=category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }

}
