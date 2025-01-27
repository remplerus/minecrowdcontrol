package com.racerxdl.minecrowdcontrol;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.racerxdl.minecrowdcontrol.Tools.makeTranslation;

public class CrowdControlModConfig extends ModConfig {
    private static final Logger Log = LogManager.getLogger();

    public CrowdControlModConfig(ModContainer container) {
        super(Type.COMMON, configSpec, container);
        Log.info("Created ModConfig instance");
    }

    public static final ForgeConfigSpec.ConfigValue<Boolean> ModEnabled;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowEffectMessages;
    public static final ForgeConfigSpec configSpec;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("General");
        ModEnabled = builder
                .comment(makeTranslation("enable.crowdcontrol"))
                .define("enableMod", true);
        ShowEffectMessages = builder
                .comment(makeTranslation("enable.crowdcontrol.effects"))
                .define("showEffectMessages", true);
        builder.pop();
        configSpec = builder.build();
    }
}
