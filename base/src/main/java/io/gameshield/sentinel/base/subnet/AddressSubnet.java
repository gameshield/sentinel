package io.gameshield.sentinel.base.subnet;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

/**
 * @author milansky
 */
public interface AddressSubnet {
    int getMaskBits();

    long size();

    boolean contains(@NotNull InetAddress address);
}
