package com.bang9634.common.util;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Utility class for IP address extraction and masking.
 */
public class IpUtil {

    /**
     * Extract IP address from WebSocket session.
     * 
     * @param session The WebSocket session
     * @return The extracted IP address as a String
     */
    public static String extractIpAddress(Session session) {
        if (session == null || session.getRemoteAddress() == null) {
            return "unknown";
        }

        String fullAddress = session.getRemoteAddress().toString();

        // Extract IP address from the full address string
        String ip = fullAddress.substring(fullAddress.lastIndexOf('[') + 1, fullAddress.lastIndexOf(']'));
        
        // Convert IPv6 localhost to IPv4 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    /**
     * Mask the given IP address for privacy.
     * 
     * @param ip The IP address to mask
     * @return The masked IP address
     */
    public static String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }

        String[] parts = ip.split("\\.");
        
        // if the IP address is not in IPv4 format, return as is
        if (parts.length != 4) {
            return ip.substring(0, Math.min(ip.length(), 20)) + "...";
        }
        return parts[0] + "." + parts[1] +".***.***";
    }

    /**
     * Get a display-friendly version of the IP address.
     * 
     * @param ip The IP address
     * @return The display-friendly IP address (ex. (192.168.***))
     */
    public static String getDisplayIp(String ip) {
        String masked = maskIpAddress(ip);
        return "(" + masked + ")";
    }

    /**
     * Check if two IP addresses are in the same subnet (assuming /16 subnet).
     * 
     * @param ip1 The first IP address
     * @param ip2 The second IP address
     * @return True if both IPs are in the same subnet, false otherwise
     */
    public static boolean isSameSubnet(String ip1, String ip2) {
        if (ip1 == null || ip2 == null) {
            return false;
        }

        String[] parts1 = ip1.split("\\.");
        String[] parts2 = ip2.split("\\.");

        if (parts1.length != 4 || parts2.length != 4) {
            return false;
        }

        // Compare first two octets for /16 subnet
        return parts1[0].equals(parts2[0]) && parts1[1].equals(parts2[1]);
    }

    private IpUtil() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate IpUtil");
    }
}
