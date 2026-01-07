package com.bang9634.user.service;

import com.google.inject.Singleton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IpBlockService {
    private static final Logger logger = LoggerFactory.getLogger(IpBlockService.class);

    private final Set<String> blockedIps;

    public IpBlockService() {
        this.blockedIps = ConcurrentHashMap.newKeySet();
        logger.info("ipBlockService initialized.");
    }

    public void blockIp(String ip) {
        if (ip != null && !ip.equals("unknown")) {
            blockedIps.add(ip);
            logger.warn("Blocked IP address: {}", ip);
        }
    }

    public void unblockIp(String ip) {
        if (blockedIps.remove(ip)) {
            logger.info("Unblocked IP address: {}", ip);
        }
    }

    public boolean isBlocked(String ip) {
        return ip != null & blockedIps.contains(ip);
    }

    public Set<String> getBlockedIps() {
        return Set.copyOf(blockedIps);
    }
}
