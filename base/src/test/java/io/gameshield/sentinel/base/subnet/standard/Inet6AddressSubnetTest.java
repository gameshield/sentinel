package io.gameshield.sentinel.base.subnet.standard;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author milansky
 */
class Inet6AddressSubnetTest {
    @Test
    @SneakyThrows
    @DisplayName("parse: without mask = /128")
    void parse_noMask_means128() {
        val subnet = Inet6AddressSubnet.parse("2001:db8::1");

        assertEquals(128, subnet.getMaskBits());

        assertTrue(subnet.contains(InetAddress.getByName("2001:db8::1")));
        assertFalse(subnet.contains(InetAddress.getByName("2001:db8::2")));
    }

    @Test
    @SneakyThrows
    @DisplayName("parse: with mask /64")
    void parse_withMask64() {
        val subnet = Inet6AddressSubnet.parse("2001:db8:0:1::/64");

        assertEquals(64, subnet.getMaskBits());

        assertTrue(subnet.contains(InetAddress.getByName("2001:db8:0:1::")));
        assertTrue(subnet.contains(InetAddress.getByName("2001:db8:0:1::1234")));
        assertFalse(subnet.contains(InetAddress.getByName("2001:db8:0:2::1")));
    }

    @Test
    @SneakyThrows
    @DisplayName("create: from Inet6Address and mask")
    void create_fromInet6Address() {
        val address = (Inet6Address) InetAddress.getByName("2001:db8::dead:beef");
        val subnet = Inet6AddressSubnet.create(address, 120);

        assertEquals(120, subnet.getMaskBits());

        assertTrue(subnet.contains(InetAddress.getByName("2001:db8::dead:beef")));
        assertFalse(subnet.contains(InetAddress.getByName("2001:db8::dead:ceef")));
    }

    @Test
    @SneakyThrows
    @DisplayName("contains: Inet4Address")
    void contains_rejectsIpv4() {
        val subnet = Inet6AddressSubnet.parse("2001:db8::/32");
        assertFalse(subnet.contains(InetAddress.getByName("127.0.0.1")));
    }

    @Test
    @DisplayName("parse: not a IPv6")
    void parse_notIpv6() {
        val exception = assertThrows(
                IllegalArgumentException.class,
                () -> Inet6AddressSubnet.parse("192.168.0.1/32")
        );
        assertTrue(exception.getMessage().contains("only IPv6 supported"));
    }

    @Test
    @DisplayName("parse: bad mask")
    void parse_badMask() {
        assertThrows(IllegalArgumentException.class,
                () -> Inet6AddressSubnet.parse("2001:db8::/0"));
        assertThrows(IllegalArgumentException.class,
                () -> Inet6AddressSubnet.parse("2001:db8::/129"));
    }

    @Test
    @DisplayName("size: small net gives a precise size")
    void size_small() {
        val subnet = Inet6AddressSubnet.parse("2001:db8::/120");
        assertEquals(256L, subnet.size());
    }

    @Test
    @DisplayName("size: cutting huge nets by Long.MAX_VALUE")
    void size_big() {
        val subnet = Inet6AddressSubnet.parse("2001:db8::/48");
        assertEquals(Long.MAX_VALUE, subnet.size());
    }
}
