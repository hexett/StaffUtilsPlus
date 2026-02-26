package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.alts.AltAccountService;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AltAccountServiceImpl.
 * Tests IP tracking and alternative account detection functionality.
 */
@ExtendWith(MockitoExtension.class)
public class AltAccountServiceImplTest {

    @Mock
    private Database mockDatabase;

    @Mock
    private Plugin mockPlugin;

    private AltAccountService altService;
    private UUID testPlayer;
    private String testIP;

    @BeforeEach
    public void setUp() {
        altService = new AltAccountServiceImpl(mockDatabase, mockPlugin);
        testPlayer = UUID.randomUUID();
        testIP = "192.168.1.100";
    }

    @Test
    public void testGetPlayerIP() {
        // Arrange
        when(mockDatabase.getPlayerIP(testPlayer)).thenReturn(testIP);

        // Act
        String result = altService.getPlayerIP(testPlayer);

        // Assert
        assertEquals(testIP, result);
        verify(mockDatabase, times(1)).getPlayerIP(eq(testPlayer));
    }

    @Test
    public void testGetPlayerIPNotFound() {
        // Arrange
        when(mockDatabase.getPlayerIP(testPlayer)).thenReturn(null);

        // Act
        String result = altService.getPlayerIP(testPlayer);

        // Assert
        assertNull(result);
    }

    @Test
    public void testGetAltAccounts() {
        // Arrange
        UUID alt1 = UUID.randomUUID();
        UUID alt2 = UUID.randomUUID();
        List<UUID> expectedAlts = new ArrayList<>();
        expectedAlts.add(testPlayer);
        expectedAlts.add(alt1);
        expectedAlts.add(alt2);

        when(mockDatabase.getPlayersByIP(testIP)).thenReturn(expectedAlts);
        when(mockDatabase.getPlayerIP(testPlayer)).thenReturn(testIP);

        // Act
        List<UUID> result = altService.getAltAccounts(testPlayer);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains(testPlayer));
        assertTrue(result.contains(alt1));
        assertTrue(result.contains(alt2));
    }

    @Test
    public void testGetPlayersByIP() {
        // Arrange
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        List<UUID> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(player1);
        expectedPlayers.add(player2);

        when(mockDatabase.getPlayersByIP(testIP)).thenReturn(expectedPlayers);

        // Act
        List<UUID> result = altService.getPlayersByIP(testIP);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    public void testGetPlayersByIPEmpty() {
        // Arrange
        when(mockDatabase.getPlayersByIP(testIP)).thenReturn(new ArrayList<>());

        // Act
        List<UUID> result = altService.getPlayersByIP(testIP);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAltAccountsSingleAccount() {
        // Arrange
        List<UUID> singleAccount = new ArrayList<>();
        singleAccount.add(testPlayer);

        when(mockDatabase.getPlayerIP(testPlayer)).thenReturn(testIP);
        when(mockDatabase.getPlayersByIP(testIP)).thenReturn(singleAccount);

        // Act
        List<UUID> result = altService.getAltAccounts(testPlayer);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(testPlayer));
    }

    @Test
    public void testValidIPFormat() {
        // Test various IP formats are handled correctly
        String[] validIPs = {
                "192.168.1.1",
                "10.0.0.1",
                "172.16.0.1",
                "8.8.8.8"
        };

        for (String ip : validIPs) {
            when(mockDatabase.getPlayersByIP(ip)).thenReturn(new ArrayList<>());
            List<UUID> result = altService.getPlayersByIP(ip);
            assertNotNull(result);
            verify(mockDatabase).getPlayersByIP(ip);
        }
    }
}
