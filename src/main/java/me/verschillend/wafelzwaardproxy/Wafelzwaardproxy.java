package me.verschillend.wafelzwaardproxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Plugin(id = "wafelzwaardproxy", name = "wafelzwaardproxy", version = "1.0-SNAPSHOT", description = "wafelzwaardproxy", authors = {"Verschillend"})
public class Wafelzwaardproxy {

    private HikariDataSource dataSource;

    @Inject
    private ProxyServer server;

    @Inject
    private Logger logger;

    @Inject
    public Wafelzwaardproxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        setupDatabase();
        logger.info("Wafelzwaardproxy initialized.");
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        int totalPlayers = getTotalPlayerCount();
        logger.info("Total player count from DB: {}", totalPlayers);

        ServerPing original = event.getPing();
        ServerPing.Players players = original.getPlayers().orElse(new ServerPing.Players(0, 1000, java.util.List.of()));

        ServerPing newPing = original.asBuilder()
                .onlinePlayers(totalPlayers)
                .maximumPlayers(players.getMax())
                .build();

        event.setPing(newPing);
    }



    private void setupDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://192.168.1.242:3306/wafelz?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername("luckperms");
            config.setPassword("");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config); // initialize manually
            logger.info("Connected to database.");
        } catch (Exception e) {
            logger.error("Failed to connect to database", e);
        }
    }

    private int getTotalPlayerCount() {
        String sql = "SELECT SUM(player_count) AS total FROM server_player_counts";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
            return 0;
        } catch (SQLException e) {
            logger.error("Failed to get total player count", e);
        }
        return 0;
    }
}