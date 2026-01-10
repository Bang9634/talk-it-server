package com.bang9634.user.service;

import com.google.inject.Singleton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing blocked IP addresses.
 * Provides methods to block, unblock, and check blocked IPs.
 */
@Singleton
public class IpBlockService {
    private static final Logger logger = LoggerFactory.getLogger(IpBlockService.class);

    // Set to store blocked IP addresses TODO: Consider database storage for persistence
    private final Set<String> blockedIps;

    /**
     * Constructor.
     */
    public IpBlockService() {
        this.blockedIps = ConcurrentHashMap.newKeySet();
        logger.info("ipBlockService initialized.");
    }

    /**
     * Block an IP address.
     * 
     * @param ip The IP address to block
     */
    public void blockIp(String ip) {
        if (ip != null && !ip.equals("unknown")) {
            blockedIps.add(ip);
            logger.warn("Blocked IP address: {}", ip);
        }
    }

    /**
     * Unblock an IP address.
     * 
     * @param ip The IP address to unblock
     */
    public void unblockIp(String ip) {
        if (blockedIps.remove(ip)) {
            logger.info("Unblocked IP address: {}", ip);
        }
    }

    /**
     * Check if an IP address is blocked.
     * 
     * @param ip The IP address to check
     * @return True if the IP is blocked, false otherwise
     */
    public boolean isBlocked(String ip) {
        return ip != null & blockedIps.contains(ip);
    }

    /**
     * Get the set of blocked IP addresses.
     * @return
     */
    public Set<String> getBlockedIps() {
        return Set.copyOf(blockedIps);
    }
}
