package com.racerxdl.minecrowdcontrol;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Config(modid = ModData.MODID, name = "minecrowdcontrol", category = "minecrowdcontrol")
public class CrowdControlModConfig {
    private static final Logger Log = LogManager.getLogger();

    @Config.Comment("should the mod be enabled?")
    public static boolean enableMod = true;
    @Config.Comment("should effect messages be send?")
    public static boolean showEffectMessages = true;

    @Mod.EventBusSubscriber
    static class ConfigurationHolder {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(ModData.MODID)) {
                ConfigManager.load(ModData.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
