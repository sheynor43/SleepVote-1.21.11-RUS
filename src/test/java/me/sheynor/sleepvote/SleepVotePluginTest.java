package me.sheynor.sleepvote;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.World;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SleepVotePluginTest {

    private ServerMock server;
    private SleepVotePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(SleepVotePlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void bedEnterInOverworldStartsVote() {
        WorldMock world = new WorldMock(org.bukkit.Material.GRASS_BLOCK, 3);
        world.setEnvironment(World.Environment.NORMAL);
        server.addWorld(world);

        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());

        PlayerBedEnterEvent event = new PlayerBedEnterEvent(player, world.getBlockAt(0, 0, 0),
                PlayerBedEnterEvent.BedEnterResult.OK);
        server.getPluginManager().callEvent(event);

        assertTrue(getVoteManager().isVoting(), "Vote should start when a bed is entered successfully in the Overworld");
    }

    @Test
    void bedEnterInNetherDoesNotStartVote() {
        WorldMock world = new WorldMock(org.bukkit.Material.NETHERRACK, 3);
        world.setEnvironment(World.Environment.NETHER);
        server.addWorld(world);

        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());

        // In the Nether/End the bed explodes instead of being used, so Bukkit reports
        // a non-OK result even though the event still fires.
        PlayerBedEnterEvent event = new PlayerBedEnterEvent(player, world.getBlockAt(0, 0, 0),
                PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE);
        server.getPluginManager().callEvent(event);

        assertFalse(getVoteManager().isVoting(), "Bed exploding in the Nether must not start a vote");
    }

    @Test
    void voteSucceedsWhenEnoughPlayersVoteYes() {
        WorldMock world = new WorldMock(org.bukkit.Material.GRASS_BLOCK, 3);
        world.setEnvironment(World.Environment.NORMAL);
        // Close to midnight, so only a handful of ticks are needed to reach daytime once skipped.
        world.setTime(23000L);
        server.addWorld(world);

        PlayerMock starter = server.addPlayer();
        starter.teleport(world.getSpawnLocation());

        PlayerBedEnterEvent event = new PlayerBedEnterEvent(starter, world.getBlockAt(0, 0, 0),
                PlayerBedEnterEvent.BedEnterResult.OK);
        server.getPluginManager().callEvent(event);

        assertTrue(getVoteManager().isVoting());

        getVoteManager().vote(starter, true);

        // 300 ticks for the 15s vote countdown, plus a safety margin for the time-skip loop.
        server.getScheduler().performTicks(400L);

        assertFalse(getVoteManager().isVoting(), "Vote should have finished after its duration elapsed");
        assertTrue(world.getTime() < 12001L, "Night should have been skipped once the vote succeeded");
    }

    @Test
    void playerCannotVoteTwice() {
        WorldMock world = new WorldMock(org.bukkit.Material.GRASS_BLOCK, 3);
        world.setEnvironment(World.Environment.NORMAL);
        server.addWorld(world);

        PlayerMock starter = server.addPlayer();
        starter.teleport(world.getSpawnLocation());

        PlayerBedEnterEvent event = new PlayerBedEnterEvent(starter, world.getBlockAt(0, 0, 0),
                PlayerBedEnterEvent.BedEnterResult.OK);
        server.getPluginManager().callEvent(event);

        getVoteManager().vote(starter, true);
        starter.nextMessage();
        getVoteManager().vote(starter, true);

        assertNotNull(starter.nextMessage(), "Voting a second time should send an already-voted message");
    }

    private VoteManager getVoteManager() {
        try {
            var field = SleepVotePlugin.class.getDeclaredField("voteManager");
            field.setAccessible(true);
            return (VoteManager) field.get(plugin);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
