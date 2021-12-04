package com.racerxdl.minecrowdcontrol;

import net.minecraft.ChatFormatting;

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
    public static final String ClientGiveHeart = ChatFormatting.GREEN + makeTranslation("client.give_heart");
    public static final String ClientTakeHeart = ChatFormatting.RED + makeTranslation("client.take_heart");
    public static final String ClientGiveFood = ChatFormatting.GREEN + makeTranslation("client.give_food");
    public static final String ClientTakeFood = ChatFormatting.RED + makeTranslation("client.take_food");
    public static final String ClientSetTimeDay = ChatFormatting.GOLD + makeTranslation("client.set_day");
    public static final String ClientSetTimeNight = ChatFormatting.GOLD + makeTranslation("client.set_night");
    public static final String ClientSetFire = ChatFormatting.RED + makeTranslation("client.set_on_fire");
    public static final String ClientKill = ChatFormatting.RED + makeTranslation("client.kill");
    public static final String ClientSendPlayerToSpawnPoint = ChatFormatting.GOLD + makeTranslation("client.player_2_spawn_point");
    public static final String ClientTakeAllHeartsButHalf = ChatFormatting.RED + makeTranslation("client.almost_dead");
    public static final String ClientFillAllHearts = ChatFormatting.GREEN + makeTranslation("client.heal");
    public static final String ClientSpawn = ChatFormatting.GOLD + makeTranslation("client.spawn_entity");
    public static final String ClientSpawnPeaceful = ChatFormatting.GOLD + makeTranslation("client.spawn_entity.peaceful");
    public static final String ClientInvertMouse = ChatFormatting.RED + makeTranslation("client.invert_mouse");
    public static final String ClientRestoreInvertMouse = ChatFormatting.GREEN + makeTranslation("client.restore_mouse");
    public static final String ClientTakeAllFood = ChatFormatting.RED + makeTranslation("client.take_all_food");
    public static final String ClientFillFood = ChatFormatting.GREEN + makeTranslation("client.fill_all_food");
    public static final String ClientJumpDisabled = ChatFormatting.RED + makeTranslation("client.jump_disabled");
    public static final String ClientJumpRestored = ChatFormatting.GREEN + makeTranslation("client.jump_restored");
    public static final String ClientMakeItRain = ChatFormatting.GOLD + makeTranslation("client.make_it_rain");
    public static final String ClientRainRestored = ChatFormatting.GOLD + makeTranslation("client.rain_restored");
    public static final String ClientGottaGoFast = ChatFormatting.YELLOW + makeTranslation("client.fast_af_boi");
    public static final String ClientGottaGoFastRestored = makeTranslation("client.not_fast_af_boi");
    public static final String ClientDrunkModeStarted = makeTranslation("client.drunk_mode_enabled");
    public static final String ClientDrunkModeRestored = makeTranslation("client.drunk_mode_disabled");
    public static final String ClientDestroyItem = ChatFormatting.RED + makeTranslation("client.destroy_item");
    public static final String ClientDropItem = ChatFormatting.RED + makeTranslation("client.drop_item");
    public static final String ClientRepairItem = ChatFormatting.RED + makeTranslation("client.repair_item");
    public static final String ClientCreateItem = ChatFormatting.GREEN + makeTranslation("client.create_item");
    public static final String ClientSetDifficult = ChatFormatting.GOLD + makeTranslation("client.set_difficulty");
    // endregion
}
