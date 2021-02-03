package dev.yxy.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            //在响应中写入图片
            ImageIO.write(image, "jpeg", response.getOutputStream());
        } catch (IOException e) {
            logger.error("验证码图片写入错误", e);
        }
    }

    //缓存控制，解决浏览器无法二次请求的问题
    @GetMapping("/no-cache")
    public void getNoCache(HttpServletResponse response) {
        BufferedImage image = defaultKaptcha.createImage("cache");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            ImageIO.write(image, "jpeg", response.getOutputStream());
        } catch (IOException e) {
            logger.error("图片写入错误", e);
        }
    }

    //随机数，即使原来的请求被缓存了，但是新请求加了随机数，不会被认为是缓存资源
    @GetMapping("/random")
    public ResponseEntity<byte[]> getRandom() {
        BufferedImage image = defaultKaptcha.createImage("random");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpeg", os);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return null;
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
