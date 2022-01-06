package com.racerxdl.minecrowdcontrol;

import net.minecraft.entity.player.EntityPlayer;

@FunctionalInterface
public interface PlayerRunnable {
    boolean run(EntityPlayer player);
}