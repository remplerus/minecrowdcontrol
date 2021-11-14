package com.racerxdl.minecrowdcontrol;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.xml.soap.Text;

public class Messages {

    // region Server Messages
    public static final String ServerGiveHeart = makeTranslation("server.give_heart");
    public static final String ServerTakeHeart = makeTranslation("server.take_heart");
    public static final String ServerGiveFood = makeTranslation("server.give_food");
    public static final String ServerTakeFood = makeTranslation("server.take_food");
    public static final String ServerSetTimeDay = makeTranslation("server.set_day");
    public static final String ServerSetTimeNight = makeTranslation("server.set_night");
    public static final String ServerSetFire = makeTranslation("server.set_on_fire");
    public static final String ServerKill = makeTranslation("server.kill");
    public static final String ServerSendPlayerToSpawnPoint = makeTranslation("server.player_2_spawn_point");
    public static final String ServerTakeAllHeartsButHalf = makeTranslation("server.almost_dead");
    public static final String ServerFillAllHearts = makeTranslation("server.heal");
    public static final String ServerSpawn = makeTranslation("server.spawn_entity");
    public static final String ServerInvertMouse = makeTranslation("server.invert_mouse");
    public static final String ServerRestoreInvertMouse = makeTranslation("server.restore_mouse");
    public static final String ServerTakeAllFood = makeTranslation("server.take_all_food");
    public static final String ServerFillFood = makeTranslation("server.fill_all_food");
    public static final String ServerJumpDisabled = makeTranslation("server.jump_disabled");
    public static final String ServerJumpRestored = makeTranslation("server.jump_restored");
    public static final String ServerMakeItRain = makeTranslation("server.make_it_rain");
    public static final String ServerRainRestored = makeTranslation("server.rain_restored");
    public static final String ServerGottaGoFast = makeTranslation("server.fast_af_boi");
    public static final String ServerGottaGoFastRestored = makeTranslation("server.not_fast_af_boi");
    public static final String ServerDrunkModeStarted = makeTranslation("server.drunk_mode_enabled");
    public static final String ServerDrunkModeRestored = makeTranslation("server.drunk_mode_disabled");
    public static final String ServerDestroyItem = makeTranslation("server.destroy_item");
    public static final String ServerDropItem = makeTranslation("server.drop_item");
    public static final String ServerRepairItem = makeTranslation("server.repair_item");
    public static final String ServerCreateItem = makeTranslation("server.create_item");
    public static final String ServerSetDifficult = makeTranslation("server.set_difficulty");
    // endregion

    // region Client Messages
    public static final String ClientGiveHeart = TextFormatting.GREEN + makeTranslation("Viewer {0} just gave a heart");
    public static final String ClientTakeHeart = TextFormatting.RED + makeTranslation("Viewer {0} just took a heart");
    public static final String ClientGiveFood = TextFormatting.GREEN + makeTranslation("Viewer {0} just gave a food point");
    public static final String ClientTakeFood = TextFormatting.RED + makeTranslation("Viewer {0} just took a food point");
    public static final String ClientSetTimeDay = TextFormatting.GOLD + makeTranslation("Viewer {0} just set the time to day");
    public static final String ClientSetTimeNight = TextFormatting.GOLD + makeTranslation("Viewer {0} just set the time to night");
    public static final String ClientSetFire = TextFormatting.RED + makeTranslation("Viewer {0} just set fire");
    public static final String ClientKill = TextFormatting.RED + makeTranslation("Viewer {0} just killed you");
    public static final String ClientSendPlayerToSpawnPoint = TextFormatting.GOLD + makeTranslation("Viewer {0} just sent you to your spawn point");
    public static final String ClientTakeAllHeartsButHalf = TextFormatting.RED + makeTranslation("Viewer {0} just took all your hearts but half");
    public static final String ClientFillAllHearts = TextFormatting.GREEN + makeTranslation("Viewer {0} just filled all your hearts");
    public static final String ClientSpawn = TextFormatting.GOLD + makeTranslation("Viewer {0} just spawned a {1}");
    public static final String ClientInvertMouse = TextFormatting.RED + makeTranslation("Viewer {0} just inverted your mouse");
    public static final String ClientRestoreInvertMouse = TextFormatting.GREEN + makeTranslation("Your mouse inversion has been restored");
    public static final String ClientTakeAllFood = TextFormatting.RED + makeTranslation("Viewer {0} just took all your food points. Now you're hungry!");
    public static final String ClientFillFood = TextFormatting.GREEN + makeTranslation("Viewer {0} just filled all your food points");
    public static final String ClientJumpDisabled = TextFormatting.RED + makeTranslation("Viewer {0} just disabled your jump");
    public static final String ClientJumpRestored = TextFormatting.GREEN + makeTranslation("Your jump is enabled again");
    public static final String ClientMakeItRain = TextFormatting.GOLD + makeTranslation("Viewer {0} just made it rain");
    public static final String ClientRainRestored = TextFormatting.GOLD + makeTranslation("The rain just stopped");
    public static final String ClientGottaGoFast = TextFormatting.YELLOW + makeTranslation("Viewer {0} just made you fast");
    public static final String ClientGottaGoFastRestored = makeTranslation("Your speed is now normal");
    public static final String ClientDrunkModeStarted = makeTranslation("Viewer {0} just made you drunk");
    public static final String ClientDrunkModeRestored = makeTranslation("You're not drunk anymore");
    public static final String ClientDestroyItem = TextFormatting.RED + makeTranslation("Viewer {0} just destroyed your {1}");
    public static final String ClientDropItem = TextFormatting.RED + makeTranslation("Viewer {0} just dropped your {1}");
    public static final String ClientRepairItem = TextFormatting.RED + makeTranslation("Viewer {0} just repaired your {1}");
    public static final String ClientCreateItem = TextFormatting.GREEN + makeTranslation("Viewer {0} just gave you a {1}");
    public static final String ClientSetDifficult = TextFormatting.GOLD + makeTranslation("Viewer {0} just set the game's difficult to {1}");
    // endregion

    public static String makeTranslation(String message) {
        return new TranslationTextComponent(message).getString();
    }
}
