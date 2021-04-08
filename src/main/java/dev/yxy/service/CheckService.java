package dev.yxy.service;

import dev.yxy.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by atom on 2021-04-08
 */
@Service("cs")
public class CheckService {
    private static final Logger logger = LoggerFactory.getLogger(CheckService.class);

    public boolean hasPermission(Member member) {
        logger.warn(member.getUsername());
        return true;
    }
}
