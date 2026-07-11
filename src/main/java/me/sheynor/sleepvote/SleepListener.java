package me.sheynor.sleepvote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class SleepListener implements Listener {

    private final VoteManager voteManager;

    public SleepListener(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        if (voteManager.isVoting()) {
            return;
        }
        voteManager.startVote(event.getPlayer());
    }
}
