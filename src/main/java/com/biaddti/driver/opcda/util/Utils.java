package com.biaddti.driver.opcda.util;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {


    @SneakyThrows
    public static String readFile(String filename) {
        //文件字节流获取文件
        InputStream is = Files.newInputStream(Paths.get(filename));
        // 将字节输入流转化成字符输入流，并设置编码格式，InputStreamReader为 Reader 的子类
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        // 使用 BufferedReader 进行读取
        BufferedReader br = new BufferedReader(isr);
        StringBuilder content = new StringBuilder();
        String line;
        //网友推荐更加简洁的写法
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        br.close();

        return content.toString();
    }
}
