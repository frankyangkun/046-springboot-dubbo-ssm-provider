package com.frank.springboot.mapper;

import com.frank.springboot.model.Student;
import org.springframework.stereotype.Component;

@Component //为了让StudentServiceImpl中studentMapper注入不报红，不加此注解也没关系
public interface StudentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Student record);

    int insertSelective(Student record);

    Student selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Student record);

    int updateByPrimaryKey(Student record);

    /**
     * 查询学生总人数
     * @return
     */
    Integer selectAllStudentCount();
}