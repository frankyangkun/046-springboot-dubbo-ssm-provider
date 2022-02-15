package com.frank.springboot.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.frank.springboot.mapper.StudentMapper;
import com.frank.springboot.model.Student;
import com.frank.springboot.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Service(interfaceClass = StudentService.class, version = "1.0.0",timeout = 15000)
@Slf4j
public class StudentServiceImpl implements StudentService {

    //注入持久层
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Student queryStudentById(Integer id) {
        log.debug("服务提供者，DAO接口实现类调用SelectByPrimaryKey()debug日志测试。");
        log.error("服务提供者，DAO接口实现类调用SelectByPrimaryKey()error日志测试。");
        return studentMapper.selectByPrimaryKey(id);
    }

    /**
     * redis存数据方法实现
     * @param id
     * @param object
     */
    @Override
    public void putRedis(Integer id, Object object) {
        redisTemplate.opsForValue().set(id,object);
    }

    /**
     * 根据学生id从redis查询详情（从缓存查询）
     * @param id
     * @return
     */
    @Override
    public Student queryStudentByRedis(Integer id) {
        Student student = (Student) redisTemplate.opsForValue().get(id);
        if(null == student){
            student = studentMapper.selectByPrimaryKey(id);
            //先写入mq，假设后面存入redis是很耗时的操作，在执行完成之前先将结果存入mq，方便后面有要用到mq数据的消费者，再进行后续插入redis操作
            testWork2();

            redisTemplate.opsForValue().set(id,student,30, TimeUnit.SECONDS);
            log.info("服务提供端：根据学生id查询详情，并存入redis。");
        }
        log.info("服务提供端：根据学生id从redis缓存查询详情。");
        return student;
    }

    //第二种模型：工作队列，默认多条消息平均分给多个消费者消费
    public void testWork(){
        for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend("microservice-work","生产端消息：微服务-work模型" + i);
        }
//        rabbitTemplate.convertAndSend("microservice-work","微服务生产端rabbitmq消息-work模型");
    }

    //第四种模型：Routing之订阅模型-Topic动态路由
    public void testWork2(){
        for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend("MicroServiceDemo_routingTopics_Exchange","user.info","生产端消息：微服务-Routing之订阅模型-Topic动态路由" + i);
        }
    }

    /**
     * 查询学生总人数，先从redis查，如果没有，从mysql查，并存入redis
     * @return
     */
    @Override
    public Integer queryStudentCount() {
        Integer allStudentCount = (Integer) redisTemplate.opsForValue().get("allStudentCount");
        if(null == allStudentCount){
            allStudentCount = studentMapper.selectAllStudentCount();
            redisTemplate.opsForValue().set("allStudentCount", allStudentCount,30,TimeUnit.SECONDS);//存入redis，k,v可以是任何类型
            log.info("服务提供者端：查询学生总人数，并存入缓存redis中。。。");
        }
        log.info("服务提供端：查询学生总人数，从缓存redis中获取。。。");

        return allStudentCount; //如果redis缓存中有数据，直接返回结果，不进入if
    }
}
