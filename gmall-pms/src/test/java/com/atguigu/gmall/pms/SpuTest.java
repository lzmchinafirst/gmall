package com.atguigu.gmall.pms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class SpuTest {
    @Test
    void test01() {
        List<user> users = Arrays.asList(
            new user(1L,"柳岩",20),
            new user(2L, "小鹿",30),
            new user(3L,"宝强",40),
            new user(4L, "马蓉", 50),
            new user(5L, "宋哲", 60)
        );
        List<Object> collect = users.stream().map(user -> {
            Person person = new Person();
            person.setAge(user.getAge());
            person.setName(user.getName());
            person.setId(user.getId());
            return person;
        }).collect(Collectors.toList());
        System.out.println(collect);
        System.out.println(users.stream().map(user -> user.getAge()).reduce((a, b) -> a + b).get());
        System.out.println(users.stream().filter(user -> user.getAge() > 20).collect(Collectors.toList()));
    }
}


@Data
@NoArgsConstructor
@AllArgsConstructor
class user {
    private Long id;
    private String name;
    private Integer age;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Person {
    private Long id;
    private String name;
    private Integer age;
}