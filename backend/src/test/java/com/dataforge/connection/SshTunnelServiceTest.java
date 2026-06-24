package com.dataforge.connection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ponytail: unit test for SSH tunnel config validation only.
 * Full SSH integration test requires a running SSH server — skipped here.
 * Add with testcontainers + testsshd if needed later.
 */
class SshTunnelServiceTest {

    @Test
    void tunnelConfigWithPassword() {
        ConnectionConfig config = new ConnectionConfig();
        config.setUseSshTunnel(true);
        config.setSshHost("remote.example.com");
        config.setSshPort(22);
        config.setSshUser("admin");
        config.setSshPassword("secret");

        assertThat(config.isUseSshTunnel()).isTrue();
        assertThat(config.getSshHost()).isEqualTo("remote.example.com");
        assertThat(config.getSshPort()).isEqualTo(22);
        assertThat(config.getSshUser()).isEqualTo("admin");
        assertThat(config.getSshPassword()).isEqualTo("secret");
    }

    @Test
    void tunnelConfigWithPrivateKey() {
        ConnectionConfig config = new ConnectionConfig();
        config.setUseSshTunnel(true);
        config.setSshHost("git.example.com");
        config.setSshPort(2222);
        config.setSshUser("git");
        config.setSshPrivateKeyPath("/home/user/.ssh/id_rsa");

        assertThat(config.isUseSshTunnel()).isTrue();
        assertThat(config.getSshPrivateKeyPath()).isNotEmpty();
        assertThat(config.getSshPassword()).isNull();
    }

    @Test
    void tunnelDisabledByDefault() {
        ConnectionConfig config = new ConnectionConfig();
        assertThat(config.isUseSshTunnel()).isFalse();
    }
}
