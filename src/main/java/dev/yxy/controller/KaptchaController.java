package dev.yxy.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Nuclear on 2021/1/28
 */
@Controller
public class KaptchaController {
    private static final Logger logger = LoggerFactory.getLogger(KaptchaController.class);

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) {
        //生成随机code
        String code = defaultKaptcha.createText();

        //----  模拟Cookie-redis存验证码操作 /start ----
        String token = generateRandomString();//生成随机字符串，作为token
        //todo redis存入 key=PREFIX:token   value=code
        logger.info("存入redis  key=[自定义前缀:{}]，value=[{}]", token, code);
        Cookie cookie = new Cookie("CAPTCHA", token);//生成Cookie
        cookie.setPath("/");
        cookie.setMaxAge(300);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);//在响应中加入Cookie
        //----  模拟Cookie-redis存验证码操作 /end ----

        //----  模拟session存验证码操作 /start ----
        //todo 可以用session代替redis进行模拟
        session.setAttribute("captcha", code);
        //----  模拟session存验证码操作 /end ----

        //根据code生成图片
        BufferedImage image = defaultKaptcha.createImage(code);
        try {
            //在响应中写入图片
            ImageIO.write(image, "jpg", response.getOutputStream());
        } catch (IOException e) {
            logger.error("验证码图片写入错误", e);
        }
    }

    //生成的Token长度
    public static final int LENGTH = 32;
    //字符源
    public static final String SOURCES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

    public String generateRandomString() {
        Random random = new Random();
        char[] chars = new char[LENGTH];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = SOURCES.charAt(random.nextInt(SOURCES.length()));
        }
        return new String(chars);
    }
}
