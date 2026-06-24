package com.dataforge.connection;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SshTunnelService {

    private static final Logger log = LoggerFactory.getLogger(SshTunnelService.class);
    private final Map<Long, Session> tunnels = new ConcurrentHashMap<>();

    public int openTunnel(Long connectionId, ConnectionConfig config) {
        if (!config.isUseSshTunnel()) return config.getPort();

        Session existing = tunnels.get(connectionId);
        if (existing != null && existing.isConnected()) {
            try {
                String[] fwd = existing.getPortForwardingL();
                if (fwd.length > 0) {
                    return Integer.parseInt(fwd[0]);
                }
            } catch (Exception ignored) {
                // tunnel stale, will reopen below
            }
        }

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(config.getSshUser(), config.getSshHost(), config.getSshPort());

            if (config.getSshPrivateKeyPath() != null && !config.getSshPrivateKeyPath().isEmpty()) {
                jsch.addIdentity(config.getSshPrivateKeyPath());
            } else if (config.getSshPassword() != null && !config.getSshPassword().isEmpty()) {
                session.setPassword(config.getSshPassword());
            }

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10_000);
            int localPort = session.setPortForwardingL(0, config.getHost(), config.getPort());
            tunnels.put(connectionId, session);
            log.info("SSH tunnel opened for connection {}: localhost:{} -> {}:{}", connectionId, localPort, config.getHost(), config.getPort());
            return localPort;
        } catch (Exception e) {
            throw new RuntimeException("SSH tunnel failed: " + e.getMessage(), e);
        }
    }

    public void closeTunnel(Long connectionId) {
        Session session = tunnels.remove(connectionId);
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH tunnel closed for connection {}", connectionId);
        }
    }
}
