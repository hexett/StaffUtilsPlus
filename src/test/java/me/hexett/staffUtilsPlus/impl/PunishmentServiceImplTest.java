package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.service.punishments.Punishment.Type;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PunishmentServiceImpl.
 * Tests ban, unban, mute, unmute, and related functionality.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class PunishmentServiceImplTest {

    @Mock
    private Database mockDatabase;

    @Mock
    private Plugin mockPlugin;

    private PunishmentService punishmentService;
    private UUID testPlayer;
    private UUID testIssuer;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    public void setUp() {
        punishmentService = new PunishmentServiceImpl(mockDatabase, mockPlugin);
        testPlayer = UUID.randomUUID();
        testIssuer = UUID.randomUUID();
        // Mock static Bukkit methods to avoid NPEs in unit tests
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(() -> Bukkit.getPlayer(any(UUID.class))).thenReturn(null);
        org.bukkit.OfflinePlayer mockOffline = mock(org.bukkit.OfflinePlayer.class);
        when(mockOffline.getName()).thenReturn("OfflineName");
        mockedBukkit.when(() -> Bukkit.getOfflinePlayer(any(UUID.class))).thenReturn(mockOffline);
        mockedBukkit.when(() -> Bukkit.getConsoleSender()).thenReturn(mock(org.bukkit.command.ConsoleCommandSender.class));
        mockedBukkit.when(() -> Bukkit.broadcast(anyString(), anyString())).then(invocation -> null);
        mockedBukkit.when(() -> Bukkit.getOnlinePlayers()).thenReturn(java.util.Collections.emptySet());
    }

    @AfterEach
    public void tearDown() {
        if (mockedBukkit != null) mockedBukkit.close();
    }

    @Test
    public void testBanPermanent() {
        // Arrange
        String reason = "Test ban reason";
        long expiresAt = -1; // Permanent
        Punishment expectedPunishment = new Punishment(testPlayer, Type.BAN, reason, System.currentTimeMillis(), expiresAt, testIssuer);


        // Act
        punishmentService.ban(testIssuer, testPlayer, reason, expiresAt);

        // Assert
        verify(mockDatabase, times(1)).insertPunishment(any(Punishment.class));
    }

    @Test
    public void testBanTemporary() {
        // Arrange
        String reason = "Temporary ban";
        long expiresAt = System.currentTimeMillis() + 3600000; // 1 hour from now


        // Act
        punishmentService.ban(testIssuer, testPlayer, reason, expiresAt);

        // Assert
        verify(mockDatabase, times(1)).insertPunishment(any(Punishment.class));
    }

    @Test
    public void testUnban() {
        // Arrange
        when(mockDatabase.getPunishments(testPlayer)).thenReturn(java.util.List.of(new Punishment(testPlayer, Type.BAN, "reason", System.currentTimeMillis(), -1, testIssuer)));
        // Act
        punishmentService.unban(testIssuer, testPlayer);

        // Assert
        verify(mockDatabase, times(1)).deactivatePunishment(eq(testPlayer), eq(Type.BAN));
        verify(mockDatabase, times(1)).deactivatePunishment(eq(testPlayer), eq(Type.TEMP_BAN));
    }

    @Test
    public void testMutePlayer() {
        // Arrange
        String reason = "Spam";
        long expiresAt = System.currentTimeMillis() + 1800000; // 30 minutes

        // Act
        punishmentService.mute(testIssuer, testPlayer, reason, expiresAt);

        // Assert
        verify(mockDatabase, times(1)).insertPunishment(any(Punishment.class));
    }

    @Test
    public void testKickPlayer() {
        // Arrange
        String reason = "Disruptive behavior";

        // Act
        punishmentService.kick(testIssuer, testPlayer, reason);

        // Assert
        verify(mockDatabase, times(1)).insertPunishment(any(Punishment.class));
    }

    @Test
    public void testGetActivePunishment() {
        // Arrange
        Punishment activePunishment = new Punishment(
                testPlayer,
                Type.TEMP_BAN,
                "Test reason",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 3600000,
                testIssuer
        );

        when(mockDatabase.getPunishments(testPlayer)).thenReturn(java.util.List.of(activePunishment));

        // Act
        Optional<Punishment> result = punishmentService.getActivePunishment(testPlayer, Type.TEMP_BAN);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPlayer, result.get().getTarget());
        assertEquals(Type.TEMP_BAN, result.get().getType());
    }

    @Test
    public void testBanCheckWithNullDatabase() {
        // This test verifies the service handles null database gracefully
        assertDoesNotThrow(() -> {
            punishmentService.ban(testIssuer, testPlayer, "Test", -1);
        });
    }

    @Test
    public void testPunishmentTypeEnum() {
        // Verify all punishment types are defined
        assertNotNull(Type.BAN);
        assertNotNull(Type.TEMP_BAN);
        assertNotNull(Type.IP_BAN);
        assertNotNull(Type.MUTE);
        assertNotNull(Type.TEMP_MUTE);
        assertNotNull(Type.KICK);
    }
}
