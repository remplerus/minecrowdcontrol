package com.racerxdl.minecrowdcontrol;

import net.minecraft.util.text.TextFormatting;

import static com.racerxdl.minecrowdcontrol.Tools.makeTranslation;

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
    public static final String ClientGiveHeart = TextFormatting.GREEN + makeTranslation("client.give_heart");
    public static final String ClientTakeHeart = TextFormatting.RED + makeTranslation("client.take_heart");
    public static final String ClientGiveFood = TextFormatting.GREEN + makeTranslation("client.give_food");
    public static final String ClientTakeFood = TextFormatting.RED + makeTranslation("client.take_food");
    public static final String ClientSetTimeDay = TextFormatting.GOLD + makeTranslation("client.set_day");
    public static final String ClientSetTimeNight = TextFormatting.GOLD + makeTranslation("client.set_night");
    public static final String ClientSetFire = TextFormatting.RED + makeTranslation("client.set_on_fire");
    public static final String ClientKill = TextFormatting.RED + makeTranslation("client.kill");
    public static final String ClientSendPlayerToSpawnPoint = TextFormatting.GOLD + makeTranslation("client.player_2_spawn_point");
    public static final String ClientTakeAllHeartsButHalf = TextFormatting.RED + makeTranslation("client.almost_dead");
    public static final String ClientFillAllHearts = TextFormatting.GREEN + makeTranslation("client.heal");
    public static final String ClientSpawn = TextFormatting.GOLD + makeTranslation("client.spawn_entity");
    public static final String ClientSpawnPeaceful = TextFormatting.GOLD + makeTranslation("client.spawn_entity.peaceful");
    public static final String ClientInvertMouse = TextFormatting.RED + makeTranslation("client.invert_mouse");
    public static final String ClientRestoreInvertMouse = TextFormatting.GREEN + makeTranslation("client.restore_mouse");
    public static final String ClientTakeAllFood = TextFormatting.RED + makeTranslation("client.take_all_food");
    public static final String ClientFillFood = TextFormatting.GREEN + makeTranslation("client.fill_all_food");
    public static final String ClientJumpDisabled = TextFormatting.RED + makeTranslation("client.jump_disabled");
    public static final String ClientJumpRestored = TextFormatting.GREEN + makeTranslation("client.jump_restored");
    public static final String ClientMakeItRain = TextFormatting.GOLD + makeTranslation("client.make_it_rain");
    public static final String ClientRainRestored = TextFormatting.GOLD + makeTranslation("client.rain_restored");
    public static final String ClientGottaGoFast = TextFormatting.YELLOW + makeTranslation("client.fast_af_boi");
    public static final String ClientGottaGoFastRestored = makeTranslation("client.not_fast_af_boi");
    public static final String ClientDrunkModeStarted = makeTranslation("client.drunk_mode_enabled");
    public static final String ClientDrunkModeRestored = makeTranslation("client.drunk_mode_disabled");
    public static final String ClientDestroyItem = TextFormatting.RED + makeTranslation("client.destroy_item");
    public static final String ClientDropItem = TextFormatting.RED + makeTranslation("client.drop_item");
    public static final String ClientRepairItem = TextFormatting.RED + makeTranslation("client.repair_item");
    public static final String ClientCreateItem = TextFormatting.GREEN + makeTranslation("client.create_item");
    public static final String ClientSetDifficult = TextFormatting.GOLD + makeTranslation("client.set_difficulty");
    // endregion
}
