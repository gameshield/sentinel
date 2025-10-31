package io.gameshield.sentinel.bungee;

import io.gameshield.sentinel.base.subnet.loader.standard.GameShieldSubnetLoader;
import io.gameshield.sentinel.base.subnet.standard.MergedAddressSpace;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.channel.BungeeChannelInitializer;
import org.jetbrains.annotations.NotNull;

@Slf4j
public final class SentinelBungee extends Plugin {
    @Override
    public void onEnable() {
        val proxy = ProxyServer.getInstance();
        val bungee = (BungeeCord) proxy;

        val subnetLoader = GameShieldSubnetLoader.create();
        val addressSpace = MergedAddressSpace.create(subnetLoader.loadSubnets());

        log.info("Loaded {} addresses", addressSpace.size());

        try {
            val unsafe = bungee.unsafe();

            val original = unsafe.getFrontendChannelInitializer();
            val wrapped = SentinelBungeeChannelInitializer.create(original.getChannelAcceptor(), addressSpace);

            bungee.unsafe().setFrontendChannelInitializer(BungeeChannelInitializer.create(wrapped));

            log.info("Hooked into frontend initializer");
        } catch (final @NotNull Throwable throwable) {
            log.warn("failed to hook into frontend", throwable);
        }
    }
}