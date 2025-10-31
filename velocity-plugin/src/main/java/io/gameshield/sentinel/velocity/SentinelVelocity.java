package io.gameshield.sentinel.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import io.gameshield.sentinel.base.subnet.loader.standard.GameShieldSubnetLoader;
import io.gameshield.sentinel.base.subnet.standard.MergedAddressSpace;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

@Slf4j
@Plugin(id = "gameshield-sentinel", name = "gameshield-sentinel", version = "1.0.0")
public final class SentinelVelocity {
    private static final VarHandle VH_CONNECTION_MANAGER;

    static {
        try {
            val lookup = MethodHandles.privateLookupIn(VelocityServer.class, MethodHandles.lookup());

            VH_CONNECTION_MANAGER = lookup.findVarHandle(
                    VelocityServer.class,
                    "cm",
                    ConnectionManager.class
            );
        } catch (final @NotNull Throwable throwable) {
            log.error("Failed to create connection manager", throwable);

            throw new ExceptionInInitializerError(throwable);
        }
    }

    private final @NotNull ProxyServer proxy;

    @Inject
    public SentinelVelocity(final @NotNull ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    @SuppressWarnings("deprecation")
    public void onProxyInit(final @NotNull ProxyInitializeEvent event) {
        if (!(proxy instanceof VelocityServer velocity)) return;

        val connectionManager = (ConnectionManager) VH_CONNECTION_MANAGER.get(velocity);
        val holder = connectionManager.getServerChannelInitializer();

        val subnetLoader = GameShieldSubnetLoader.create();
        val addressSpace = MergedAddressSpace.create(subnetLoader.loadSubnets());

        log.info("Loaded {} addresses", addressSpace.size());

        holder.set(SentinelVelocityChannelInitializer.create(holder.get(), addressSpace));
    }
}
