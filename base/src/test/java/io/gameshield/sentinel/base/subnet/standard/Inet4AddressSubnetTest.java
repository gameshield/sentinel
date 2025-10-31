package io.gameshield.sentinel.base.subnet.standard;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author milansky
 */
class Inet4AddressSubnetTest {
    @Test
    @SneakyThrows
    @DisplayName("parse: cidr /24")
    void parseCidr_ok() {
        val subnet = Inet4AddressSubnet.parse("192.168.1.0/24");

        val inside = InetAddress.getByName("192.168.1.42");
        val outside = InetAddress.getByName("192.168.2.1");

        assertEquals(24, subnet.getMaskBits());
        assertTrue(subnet.contains(inside));
        assertFalse(subnet.contains(outside));
    }

    @Test
    @SneakyThrows
    @DisplayName("parse: without mask = /32")
    void parseWithoutMask_treatedAs32() {
        val subnet = Inet4AddressSubnet.parse("10.0.0.5");
        val same = InetAddress.getByName("10.0.0.5");
        val other = InetAddress.getByName("10.0.0.6");

        assertEquals(32, subnet.getMaskBits());
        assertTrue(subnet.contains(same));
        assertFalse(subnet.contains(other));
    }

    @Test
    @DisplayName("parse: IPv6 is forbidden")
    void parse_ipv6_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Inet4AddressSubnet.parse("2001:db8::1/64"));
    }

    @Test
    @DisplayName("parse: mask /0 is forbidden")
    void parse_mask0_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Inet4AddressSubnet.parse("0.0.0.0/0"));
    }

    @Test
    @DisplayName("parse: incorrect address")
    void parse_badAddress_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Inet4AddressSubnet.parse("999.999.1.1/24"));
    }
}
