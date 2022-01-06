package com.racerxdl.minecrowdcontrol;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static com.racerxdl.minecrowdcontrol.Tools.makeTranslation;

@Mod(modid = ModData.MODID, name = "Minecraft CrowdControl", version = "1.1.0", acceptedMinecraftVersions = "[1.12, 1.13)", useMetadata = true)
@Mod.EventBusSubscriber
public class MineCrowdControl {
    private static final Logger Log = LogManager.getLogger();

    @Mod.Instance
    public static MineCrowdControl instance;

    private ControlServer cs;
    private Minecraft client;

    public MineCrowdControl() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        Log.info("Server started. Creating Control Server");
        cs = new ControlServer(event.getServer());
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        Log.info("Server stopping. Stopping Control Server");
        cs.Stop();
        cs = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onJump(LivingEvent.LivingJumpEvent event) {
        if (cs.GetStates().getJumpDisabled()) {
            Vec3d motion = event.getEntity().getForward();
            event.getEntity().getForward().addVector(motion.x, 0, motion.z);
        }
    }

    @SubscribeEvent
    public void onWorldEntry(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            if (CrowdControlModConfig.enableMod) {
                Log.info("Player in. Starting CrowdControl");
                cs.SetClient(client);
                cs.SetPlayer((EntityPlayer) event.getEntity());

                Commands.SetEnablePlayerMessages(CrowdControlModConfig.showEffectMessages);

                cs.Start();
            } else {
                Commands.SendPlayerSystemMessage((EntityPlayer) event.getEntity(), "Crowd Control is disabled");
            }
        }
    }

    @Mod.EventHandler
    public void onConfigLoad(FMLPreInitializationEvent configEvent) {
        Log.info("Got Client");
        this.client = Minecraft.getMinecraft();
    }
}
