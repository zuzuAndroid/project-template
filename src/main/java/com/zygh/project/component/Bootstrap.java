package com.zygh.project.component;

import cn.hutool.core.date.DateUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by ZhuHongYu on 2024/12/3.
 */
@Component
public class Bootstrap implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("程序启动: " + DateUtil.now());
    }
}
