package com.fengxin.test;

import com.github.javafaker.Faker;
import com.github.javafaker.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 随机数据单元测试类
 **/
@SpringBootTest
public class FakerTest {
    @Test
    public void generateData(){
        Faker faker = new Faker(Locale.CHINA);
        // 生成中文名
        String chineseName = faker.name().fullName();
        System.out.println("中文名: " + chineseName);

        // 生成手机号
        PhoneNumber phoneNumber = faker.phoneNumber();
        String mobileNumber = phoneNumber.cellPhone();
        System.out.println("手机号: " + mobileNumber);

        // 生成电子邮箱
        String email = faker.internet().emailAddress();
        System.out.println("电子邮箱: " + email);
    }
}
