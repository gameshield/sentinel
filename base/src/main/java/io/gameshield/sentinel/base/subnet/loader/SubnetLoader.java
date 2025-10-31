package io.gameshield.sentinel.base.subnet.loader;

import io.gameshield.sentinel.base.subnet.AddressSubnet;
import org.jetbrains.annotations.NotNull;

/**
 * @author milansky
 */
public interface SubnetLoader {
    @NotNull AddressSubnet @NotNull [] loadSubnets();
}
