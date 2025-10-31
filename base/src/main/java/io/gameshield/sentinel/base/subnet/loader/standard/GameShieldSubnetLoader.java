package io.gameshield.sentinel.base.subnet.loader.standard;

import io.gameshield.sentinel.base.subnet.AddressSubnet;
import io.gameshield.sentinel.base.subnet.loader.SubnetLoader;
import io.gameshield.sentinel.base.subnet.standard.Inet4AddressSubnet;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author milansky
 */
@NoArgsConstructor(staticName = "create")
public final class GameShieldSubnetLoader implements SubnetLoader {
    private static final String SERVERS_PLAIN_URL = "https://rest.gameshield.io/v1/servers/plain";

    @Override
    public @NotNull AddressSubnet @NotNull [] loadSubnets() {
        val result = new ArrayList<AddressSubnet>();
        HttpURLConnection conn = null;
        try {
            val url = new URL(SERVERS_PLAIN_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            val code = conn.getResponseCode();
            if (code != 200) return new AddressSubnet[0];

            try (val reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    result.add(Inet4AddressSubnet.parse(line));
                }
            }
        } catch (final @NotNull IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result.toArray(new AddressSubnet[0]);
    }
}
