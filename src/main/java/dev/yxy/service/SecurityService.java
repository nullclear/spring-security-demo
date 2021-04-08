package dev.yxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by atom on 2021-04-08
 */
@Service("ss")
public class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    public boolean hasPermission(String permission) {
        logger.warn(permission);
        return true;
    }
}
