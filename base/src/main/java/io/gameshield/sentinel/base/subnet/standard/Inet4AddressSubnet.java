package io.gameshield.sentinel.base.subnet.standard;

import io.gameshield.sentinel.base.subnet.AddressSubnet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author milansky
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Inet4AddressSubnet implements AddressSubnet {
    private static boolean contains(final int address, final int subject, final int mask) {
        return (address & mask) == (subject & mask);
    }

    private static int mask(final int maskBits) {
        return -(1 << (32 - maskBits));
    }

    public static Inet4AddressSubnet create(final Inet4Address address, final int maskBits) {
        return new Inet4AddressSubnet(address.hashCode(), mask(maskBits), maskBits);
    }

    public static Inet4AddressSubnet create(final int address, final int maskBits) {
        return new Inet4AddressSubnet(address, mask(maskBits), maskBits);
    }

    public static Inet4AddressSubnet parse(final String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("value is null or empty");
        }

        final String addrPart;
        final int maskBits;
        final int slashIdx = value.indexOf('/');

        if (slashIdx < 0) {
            addrPart = value;
            maskBits = 32;
        } else {
            addrPart = value.substring(0, slashIdx);
            try {
                maskBits = Integer.parseInt(value.substring(slashIdx + 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("mask must be integer in 1..32: " + value, e);
            }
        }

        if (maskBits < 1 || maskBits > 32) {
            throw new IllegalArgumentException("unsupported mask: " + maskBits);
        }

        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(addrPart);
        } catch (final UnknownHostException exception) {
            throw new IllegalArgumentException("invalid IPv4: " + addrPart, exception);
        }

        if (!(inetAddress instanceof Inet4Address)) {
            throw new IllegalArgumentException("only IPv4 supported: " + addrPart);
        }

        return create((Inet4Address) inetAddress, maskBits);
    }

    int address, mask, maskBits;

    @Override
    public int getMaskBits() {
        return maskBits;
    }

    @Override
    public long size() {
        return 1L << (32 - maskBits);
    }

    @Override
    public boolean contains(final @NotNull InetAddress address) {
        return address instanceof Inet4Address && contains(address.hashCode());
    }

    public boolean contains(final int address) {
        return contains(this.address, address, mask);
    }
}
