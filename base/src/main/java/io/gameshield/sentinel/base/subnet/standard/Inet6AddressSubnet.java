package io.gameshield.sentinel.base.subnet.standard;

import io.gameshield.sentinel.base.subnet.AddressSubnet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author milansky
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Inet6AddressSubnet implements AddressSubnet {
    private static final int FULL_BITS = 128;

    private static boolean contains(
            final long addrHi, final long addrLo,
            final long subjHi, final long subjLo,
            final long maskHi, final long maskLo
    ) {
        return (addrHi & maskHi) == (subjHi & maskHi)
                && (addrLo & maskLo) == (subjLo & maskLo);
    }

    private static long mask64(final int bits) {
        if (bits == 0) {
            return 0L;
        }
        return -1L << (64 - bits);
    }

    private static long[] mask(final int maskBits) {
        if (maskBits < 1 || maskBits > FULL_BITS) {
            throw new IllegalArgumentException("unsupported mask: " + maskBits);
        }

        if (maskBits <= 64) {
            return new long[]{mask64(maskBits), 0L};
        } else {
            return new long[]{-1L, mask64(maskBits - 64)};
        }
    }

    private static long[] toLongs(final @NotNull Inet6Address address) {
        val bytes = address.getAddress();
        if (bytes.length != 16) throw new IllegalArgumentException("invalid IPv6 length: " + bytes.length);

        long hi = 0L, lo = 0L;
        for (int i = 0; i < 8; i++) hi = (hi << 8) | (bytes[i] & 0xFFL);
        for (int i = 8; i < 16; i++) lo = (lo << 8) | (bytes[i] & 0xFFL);

        return new long[]{hi, lo};
    }

    public static Inet6AddressSubnet create(final @NotNull Inet6Address address, final int maskBits) {
        val addressLongs = toLongs(address);
        val maskLongs = mask(maskBits);
        return new Inet6AddressSubnet(addressLongs[0], addressLongs[1], maskLongs[0], maskLongs[1], maskBits);
    }

    public static Inet6AddressSubnet create(final long addressHi, final long addressLo, final int maskBits) {
        val maskLongs = mask(maskBits);
        return new Inet6AddressSubnet(addressHi, addressLo, maskLongs[0], maskLongs[1], maskBits);
    }

    public static Inet6AddressSubnet parse(final @NotNull String value) {
        final String addrPart;
        final int maskBits;
        final int slashIdx = value.indexOf('/');

        if (slashIdx < 0) {
            addrPart = value;
            maskBits = FULL_BITS;
        } else {
            addrPart = value.substring(0, slashIdx);
            try {
                maskBits = Integer.parseInt(value.substring(slashIdx + 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("mask must be integer in 1..128: " + value, e);
            }
        }

        if (maskBits < 1 || maskBits > FULL_BITS) {
            throw new IllegalArgumentException("unsupported mask: " + maskBits);
        }

        final InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(addrPart);
        } catch (final UnknownHostException exception) {
            throw new IllegalArgumentException("invalid IPv6: " + addrPart, exception);
        }

        if (!(inetAddress instanceof Inet6Address)) {
            throw new IllegalArgumentException("only IPv6 supported: " + addrPart);
        }

        return create((Inet6Address) inetAddress, maskBits);
    }

    long addressHi, addressLo;
    long maskHi, maskLo;
    int maskBits;

    @Override
    public long size() {
        final int hostBits = FULL_BITS - maskBits;

        if (hostBits >= 63) return Long.MAX_VALUE;

        return 1L << hostBits;
    }

    @Override
    public boolean contains(final @NotNull InetAddress address) {
        return address instanceof Inet6Address && contains((Inet6Address) address);
    }

    public boolean contains(final Inet6Address address) {
        final long[] subj = toLongs(address);
        return contains(this.addressHi, this.addressLo, subj[0], subj[1], this.maskHi, this.maskLo);
    }

    public boolean contains(final long subjectHi, final long subjectLo) {
        return contains(this.addressHi, this.addressLo, subjectHi, subjectLo, this.maskHi, this.maskLo);
    }
}
