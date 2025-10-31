package io.gameshield.sentinel.base.subnet.standard;

import io.gameshield.sentinel.base.subnet.AddressSubnet;
import io.gameshield.sentinel.base.subnet.loader.SubnetLoader;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
public final class MergedAddressSpace implements AddressSubnet {
    private final AddressSubnet[] subnets;

    @Override
    public long size() {
        return Arrays.stream(subnets).mapToLong(AddressSubnet::size).sum();
    }

    @Override
    public boolean contains(final @NotNull InetAddress address) {
        return Arrays.stream(subnets).anyMatch(subnet -> subnet.contains(address));
    }
}
