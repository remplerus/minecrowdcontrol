package com.racerxdl.minecrowdcontrol;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PlayerRunnable {
    boolean run(Player player);
}