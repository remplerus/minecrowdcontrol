package com.racerxdl.minecrowdcontrol;

import com.racerxdl.minecrowdcontrol.CrowdControl.RequestType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MinecraftCommand {
    CommandResult Run(PlayerStates states, Player player, Minecraft client, MinecraftServer server, String viewer, RequestType type);
}
