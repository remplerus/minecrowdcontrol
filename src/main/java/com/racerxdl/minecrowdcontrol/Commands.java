package com.racerxdl.minecrowdcontrol;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.racerxdl.minecrowdcontrol.CrowdControl.EffectResult;
import com.racerxdl.minecrowdcontrol.CrowdControl.RequestType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.*;

public class Commands {
    private static final Logger Log = LogManager.getLogger();
    private static boolean enablePlayerMessages = false;
    private static int checkedDifficulty = 0;

    public static final Map<String, MinecraftCommand> CommandList = new HashMap<>() {{
        put("KILL", Commands::KillPlayers);
        put("TAKE_HEART", Commands::TakeHeart);
        put("GIVE_HEART", Commands::GiveHeart);
        put("SET_FIRE", Commands::SetFire);
        put("SET_TIME_NIGHT", Commands::SetTimeNight);
        put("SET_TIME_DAY", Commands::SetTimeDay);
        put("TAKE_FOOD", Commands::TakeFood);
        put("GIVE_FOOD", Commands::GiveFood);
        put("SEND_PLAYER_TO_SPAWN_POINT", Commands::SendPlayerToSpawnPoint);
        put("TAKE_ALL_HEARTS_BUT_HALF", Commands::TakeAllHeartsButHalf);
        put("FILL_HEARTS", Commands::FillHearts);
        put("INVERT_MOUSE", Commands::SetInvertMouse);
        put("DISABLE_JUMP", Commands::SetJumpDisabled);
        put("TAKE_ALL_FOOD", Commands::TakeAllFood);
        put("FILL_FOOD", Commands::FillFood);
        put("MAKE_IT_RAIN", Commands::SetRaining);
        put("GOTTA_GO_FAST", Commands::GottaGoFast);
        put("DRUNK_MODE", Commands::DrunkMode);
        put("DESTROY_SELECTED_ITEM", Commands::DestroySelectedItem);
        put("DROP_SELECTED_ITEM", Commands::DropSelectedItem);
        put("REPAIR_SELECTED_ITEM", Commands::RepairSelectedItem);
        put("EXPLODE_PLAYER", Commands::ExplodePlayer);
    }};

    private static final List<EntityType<?>> spawnEntities = new ArrayList<>(Arrays.asList(
            EntityType.CREEPER,
            EntityType.BLAZE,
            EntityType.CAVE_SPIDER,
            EntityType.WITCH,
            EntityType.BEE,
            EntityType.ZOMBIE,
            EntityType.CHICKEN,
            EntityType.PIG,
            EntityType.VILLAGER,
            EntityType.SILVERFISH,
            EntityType.RAVAGER,
            EntityType.VEX,
            EntityType.TROPICAL_FISH
    ));

    private static final List<EntityType<?>> spawnBigEntites = new ArrayList<>(Arrays.asList(
            EntityType.PHANTOM,
            EntityType.SLIME,
            EntityType.WITHER,
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.HORSE,
            EntityType.SKELETON_HORSE,
            EntityType.ZOMBIE_HORSE,
            EntityType.SPIDER,
            EntityType.ENDERMAN,
            EntityType.ENDER_DRAGON
    ));

    private static final List<Item> spawnItems = new ArrayList<>(Arrays.asList(
            Items.LEATHER,
            Items.STONE,
            Items.OAK_WOOD,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.DIAMOND,

            Items.CRAFTING_TABLE,

            Items.STONE_PICKAXE,
            Items.STONE_SWORD,
            Items.STONE_AXE,
            Items.STONE_SHOVEL,

            Items.ENDER_PEARL,

            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_AXE,
            Items.DIAMOND_HORSE_ARMOR,
            Items.DIAMOND_SHOVEL
    ));

    private static final List<Difficulty> difficults = new ArrayList<>(Arrays.asList(
            Difficulty.PEACEFUL,
            Difficulty.EASY,
            Difficulty.NORMAL,
            Difficulty.HARD
    ));

    static {
        spawnEntities.forEach((et) -> {
            String entityName = et.getDescription().getString().toUpperCase().replace(" ", "");
            Log.info("Adding command SPAWN_{}", entityName);
            CommandList.put("SPAWN_" + entityName, (states, u, u1, server, viewer, type) -> SpawnEntity(states, server, viewer, type, et));
        });

        spawnBigEntites.forEach(et -> {
            String entityName = et.getDescription().getString().toUpperCase().replace(" ", "");
            Log.info("Adding command SPAWN_{}", entityName);
            CommandList.put("SPAWN_" + entityName, (states, u, u1, server, viewer, type) -> SpawnEntity(states, server, viewer, type, et));
        });

        spawnItems.forEach((item) -> {
            String itemName = item.getDescription().getString().toUpperCase().replace(" ", "");
            Log.info("Adding command CREATE_{}", itemName);
            CommandList.put("CREATE_" + itemName, (states, player, u1, server, viewer, type) -> SpawnItem(states, player, server, viewer, type, item));
        });

        difficults.forEach((diff) -> {
            String diffName = diff.getDisplayName().getString().toUpperCase().replace(" ", "");
            Log.info("Adding command SET_DIFFICULT_{}", diffName);
            CommandList.put("SET_DIFFICULT_" + diffName, (states, player, u1, server, viewer, type) -> SetDifficult(states, player, server, viewer, type, diff));
        });
    }

    public static void SetEnablePlayerMessages(boolean status) {
        enablePlayerMessages = status;
    }

    // Run command on all players and returns if any of them returned true
    public static boolean RunOnPlayers(MinecraftServer server, PlayerRunnable runnable) {
        boolean result = false;
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (Player player : players) {
            result |= runnable.run(player);
        }
        return result;
    }

    public static void SendPlayerMessage(Player player, String msg, Object... params) {
        if (enablePlayerMessages) {
            SendPlayerSystemMessage(player, msg, params);
        }
    }

    public static void SendPlayerSystemMessage(Player player, String msg, Object... params) {
        player.displayClientMessage(new TextComponent(MessageFormat.format(msg, params)), false);
    }

    public static void SendSystemMessage(MinecraftServer server, String msg, Object... params) {
        RunOnPlayers(server, (player) -> {
            SendPlayerSystemMessage(player, msg, params);
            return true;
        });
    }

    public static CommandResult SetDifficult(PlayerStates states, Player player, MinecraftServer server, String viewer1, RequestType type, Difficulty diff) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);
        
        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop || player.getCommandSenderWorld().getLevelData().getDifficulty() == diff) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        try {
            DifficultyCommand.setDifficulty(server.createCommandSourceStack(), diff);
        } catch (CommandSyntaxException e) {
            Log.error("Got command syntax exception: " + e);
            return res.SetEffectResult(EffectResult.Failure);
        }

        Log.info(Messages.ServerSetDifficult, viewer, diff.getKey());
        SendPlayerMessage(player, Messages.ClientSetDifficult, viewer, diff.getDisplayName().getString());

        return res.SetEffectResult(EffectResult.Success);
    }


    public static CommandResult SpawnItem(PlayerStates states, Player p, MinecraftServer server, String viewer1, RequestType type, Item item) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop || p.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        ItemStack is = new ItemStack(item);
        is.setCount(1);
        p.spawnAtLocation(is);
        RunOnPlayers(server, (player -> {
            player.spawnAtLocation(is);

            Log.info(Messages.ServerCreateItem, viewer, player.getName().getString(), item.getDescription().getString());
            SendPlayerMessage(player, Messages.ClientCreateItem, viewer, item.getDescription().getString());

            return true;
        }));

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult ExplodePlayer(PlayerStates states, Player p, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            float health = player.getHealth();
            if (health != 0) {
                Log.info(Messages.ServerKill, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientKill, viewer);

                player.level.explode(player, player.getX(), player.getY(), player.getZ(), 8, Explosion.BlockInteraction.DESTROY);

                player.level.createFireworks(player.getX(), player.getY(), player.getZ(), 0, 0, 0, null);
                player.setHealth(0);
                return true;
            }

            return false;
        }));

        if (result) {
            assert client.player != null;
            client.player.playSound(SoundEvents.ENDERMAN_SCREAM, 6, 0.25f);
            p.level.createFireworks(p.getX(), p.getY(), p.getZ() + 20, 0, 0, 0, null);
            p.setDeltaMovement(2, 2, 2);
        }

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Unavailable);
    }

    public static CommandResult RepairSelectedItem(PlayerStates states, Player player, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        int itemIndex = player.getInventory().selected;
        ItemStack is = player.getInventory().items.get(itemIndex);

        if (is.isEmpty() || !is.isRepairable() || is.getDamageValue() == 0 || player.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Retry);
        }

        RunOnPlayers(server, (p) -> {
            p.getInventory().items.get(itemIndex).setDamageValue(0);
            return true;
        });

        player.getInventory().items.get(itemIndex).setDamageValue(0);

        assert client.player != null;
        client.player.playSound(SoundEvents.ANVIL_HIT, 1, 1);

        Log.info(Messages.ServerRepairItem, viewer, player.getName().getString(), is.getItem().getDescription().getString());
        SendPlayerMessage(player, Messages.ClientRepairItem, viewer, is.getItem().getDescription().getString());

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult DropSelectedItem(PlayerStates states, Player player, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        int itemIndex = player.getInventory().selected;
        ItemStack is = player.getInventory().items.get(itemIndex);

        if (is.isEmpty() || player.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Retry);
        }

        RunOnPlayers(server, (p) -> {
            p.drop(is, false);
            p.getInventory().items.set(itemIndex, ItemStack.EMPTY);
            return true;
        });

        player.getInventory().items.set(itemIndex, ItemStack.EMPTY);

        assert client.player != null;
        client.player.playSound(SoundEvents.COW_DEATH, 1, 8);

        Log.info(Messages.ServerDropItem, viewer, player.getName().getString(), is.getItem().getDescription().getString());
        SendPlayerMessage(player, Messages.ClientDropItem, viewer, is.getItem().getDescription().getString());

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult DestroySelectedItem(PlayerStates states, Player player, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        int itemIndex = player.getInventory().selected;
        ItemStack is = player.getInventory().items.get(itemIndex);
        if (is.isEmpty() || player.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Retry);
        }

        if (is.getCount() > 1) {
            int newCount = is.getCount() - 1;
            is.setCount(newCount);
            RunOnPlayers(server, (p) -> {
                p.getInventory().items.get(itemIndex).setCount(newCount);
                return true;
            });
        } else {
            player.getInventory().items.set(itemIndex, ItemStack.EMPTY);
            RunOnPlayers(server, (p) -> {
                p.getInventory().items.set(itemIndex, ItemStack.EMPTY);
                return true;
            });
        }

        assert client.player != null;
        client.player.playSound(SoundEvents.COW_HURT, 1, 8);

        Log.info(Messages.ServerDestroyItem, viewer, player.getName().getString(), is.getItem().getDescription().getString());
        SendPlayerMessage(player, Messages.ClientDestroyItem, viewer, is.getItem().getDescription().getString());

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult DrunkMode(PlayerStates states, Player player, Minecraft unused, MinecraftServer unused2, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Start) {
            if (states.getDrunkMode()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerDrunkModeStarted, viewer);
            SendPlayerMessage(player, Messages.ClientDrunkModeStarted, viewer);
            MobEffectInstance eff = new MobEffectInstance(MobEffects.CONFUSION, 60 * 21, 2); // ~21 ticks = 1 second
            player.addEffect(eff);

            return res
                    .SetNewStates(states.setDrunkMode(true))
                    .SetEffectResult(EffectResult.Success);
        } else if (type == RequestType.Stop) {
            if (!states.getDrunkMode()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerDrunkModeRestored);
            SendPlayerMessage(player, Messages.ClientDrunkModeRestored);

            player.removeEffect(MobEffects.CONFUSION);

            return res
                    .SetNewStates(states.setDrunkMode(false))
                    .SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult GottaGoFast(PlayerStates states, Player clientPlayer, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);


        boolean result = RunOnPlayers(server, (player) -> {
            if (type == RequestType.Start) {
                if (states.getGottaGoFast()) {
                    return false;
                }
                Log.info(Messages.ServerGottaGoFast, viewer);
                SendPlayerMessage(player, Messages.ClientGottaGoFast, viewer);
                MobEffectInstance eff = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 21, 10); // ~21 ticks = 1 second
                player.addEffect(eff);

                return true;
            } else if (type == RequestType.Stop) {
                if (!states.getGottaGoFast()) {
                    return false;
                }

                Log.info(Messages.ServerGottaGoFastRestored);
                SendPlayerMessage(player, Messages.ClientGottaGoFastRestored);

                player.removeEffect(MobEffects.MOVEMENT_SPEED);

                return true;
            }

            return true;
        });

        if (result) {
            res = res.SetNewStates(
                    res.GetPlayerStates()
                            .setGottaGoFast(type == RequestType.Start)
                            .setGottaGoFastViewer(viewer)
            );
        }

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetRaining(PlayerStates states, Player unused, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        boolean result = RunOnPlayers(server, (player) -> {
            Level w = player.getCommandSenderWorld();
            if (type == RequestType.Start) {
                if (w.getLevelData().isRaining()) {
                    return false;
                }

                Biome b = w.getBiome(new BlockPos(player.position()));

                if (b.getPrecipitation() == Biome.Precipitation.NONE) {
                    return false;
                }

                Log.info(Messages.ServerMakeItRain, viewer);
                SendPlayerMessage(player, Messages.ClientMakeItRain, viewer);

                Log.info(w.getLevelData().isRaining());
                w.getLevelData().setRaining(true);
                Objects.requireNonNull(w.getServer()).getWorldData().overworldData().setClearWeatherTime(0);
                w.getServer().getWorldData().overworldData().setRainTime(1200);
                w.getServer().getWorldData().overworldData().setThundering(false);
                return true;
            } else if (type == RequestType.Stop) {
                Log.info(Messages.ServerRainRestored);
                SendPlayerMessage(player, Messages.ClientRainRestored);

                w.getLevelData().setRaining(false);

                return true;
            }

            return true;
        });

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetInvertMouse(PlayerStates states, Player player, Minecraft client, MinecraftServer unused2, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalizeFully(viewer1);

        if (type == RequestType.Start) {
            if (client.options.invertYMouse) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerInvertMouse, viewer, player.getName().getString());
            SendPlayerMessage(player, Messages.ClientInvertMouse, viewer);
            client.options.invertYMouse = true;
            return res.SetEffectResult(EffectResult.Success);
        } else if (type == RequestType.Stop) {
            if (!client.options.invertYMouse) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerRestoreInvertMouse, player.getName().getString());
            SendPlayerMessage(player, Messages.ClientRestoreInvertMouse);
            client.options.invertYMouse = false;
            return res.SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult SetJumpDisabled(PlayerStates states, Player player, Minecraft client, MinecraftServer unused2, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Start) {
            if (res.GetPlayerStates().getJumpDisabled()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerJumpDisabled, viewer, player.getName().getString());
            SendPlayerMessage(player, Messages.ClientJumpDisabled, viewer);
            return res
                    .SetEffectResult(EffectResult.Success)
                    .SetNewStates(res.GetPlayerStates().setJumpDisabled(true));
        } else if (type == RequestType.Stop) {
            if (!res.GetPlayerStates().getJumpDisabled()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerJumpRestored, player.getName().getString());
            SendPlayerMessage(player, Messages.ClientJumpRestored);
            return res
                    .SetEffectResult(EffectResult.Success)
                    .SetNewStates(res.GetPlayerStates().setJumpDisabled(false));
        }

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult SetTimeNight(PlayerStates states, Player player, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        Level world = player.getCommandSenderWorld();
        if (world.getDayTime() < 13000 || world.getDayTime() > 23000) {
            Log.info(Messages.ServerSetTimeNight, viewer);
            TimeCommand.setTime(server.createCommandSourceStack(), Tools.NIGHT);
            SendPlayerMessage(player, Messages.ClientSetTimeNight, viewer);
            return res.SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Unavailable);
    }

    public static CommandResult SendPlayerToSpawnPoint(PlayerStates states, Player entity, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player) -> {
            if (player.getHealth() == 0) {
                return false;
            }
            if (player.isPassenger()) {
                return false;
            }

            try {
                BlockPos spawnPoint = ((ServerPlayer)player).getRespawnPosition(); // Believe or not, this is spawn point getter
                if (spawnPoint == null) {
                    ServerLevelData data = server.getWorldData().overworldData();
                    spawnPoint = new BlockPos(data.getXSpawn(), data.getYSpawn(), data.getZSpawn());
                    Log.info("No spawnpoint found, move to world spawn");
                }
                ServerLevel world = server.getAllLevels().iterator().next();
                if (world.dimensionType() != player.getCommandSenderWorld().dimensionType()) {
                    player.changeDimension(world, world.getPortalForcer());
                }
                player.teleportTo(spawnPoint.getX(), spawnPoint.getY()+1, spawnPoint.getZ());
            } catch (Exception e) {
                Log.error("Error getting field: " + e);
                return false;
            }

            Log.info(Messages.ServerSendPlayerToSpawnPoint, viewer, player.getName().getString());
            SendPlayerMessage(player, Messages.ClientSendPlayerToSpawnPoint, viewer);

            return true;
        });

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetTimeDay(PlayerStates states, Player player, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }
        Level world = player.getCommandSenderWorld();

        if (world.getDayTime() > 6000) {
            Log.info(Messages.ServerSetTimeDay, viewer);
            TimeCommand.setTime(server.createCommandSourceStack(), Tools.DAY);
            SendPlayerMessage(player, Messages.ClientSetTimeDay, viewer);
            return res.SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Unavailable);
    }

    public static CommandResult SpawnEntity(PlayerStates states, MinecraftServer server, String viewer1, RequestType type, EntityType<?> entityType) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player) -> {
            if (server.getWorldData().getDifficulty() == Difficulty.PEACEFUL) {
                if (checkedDifficulty == 0) {
                    SendPlayerMessage(player, Messages.ClientSpawnPeaceful, viewer);
                    checkedDifficulty = 1;
                }
                return false;
            }

            Vec3 pos = player.position();
            Entity e = entityType.create(player.getCommandSenderWorld());
            if (e != null && e.isFree(pos.x + player.getRandom().nextInt(4) - 2, pos.y + 1, pos.z + player.getRandom().nextInt(4) -2)) {
                e.absMoveTo(pos.x + player.getRandom().nextInt(4) - 2, pos.y + 1, pos.z + player.getRandom().nextInt(4) - 2, 0, 0);

                Log.info(Messages.ServerSpawn, viewer, entityType.getDescription().getString());
                SendPlayerMessage(player, Messages.ClientSpawn, viewer, entityType.getDescription().getString());

                player.getCommandSenderWorld().addFreshEntity(e);
                checkedDifficulty = 0;
            } else {
                return false;
            }

            return true;
        });

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeFood(PlayerStates states, Player player, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (p -> {
            FoodData fs = p.getFoodData();
            if (fs.getFoodLevel() > 0 && player.getHealth() != 0) {
                Log.info(Messages.ServerTakeFood, viewer, p.getName().getString());
                SendPlayerMessage(p, Messages.ClientTakeFood, viewer);
                fs.setFoodLevel(fs.getFoodLevel() - 2);
                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult GiveFood(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            FoodData fs = player.getFoodData();
            if (fs.getFoodLevel() < Tools.MAX_FOOD && player.getHealth() != 0) {
                Log.info(Messages.ServerGiveFood, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientGiveFood, viewer);
                fs.setFoodLevel(fs.getFoodLevel() + 2);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeAllHeartsButHalf(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            if (player.getHealth() > 1) {
                Log.info(Messages.ServerTakeAllHeartsButHalf, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientTakeAllHeartsButHalf, viewer);
                player.setHealth(1);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeAllFood(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            FoodData fs = player.getFoodData();
            if (fs.getFoodLevel() > 0 && player.getHealth() != 0) {
                Log.info(Messages.ServerTakeAllFood, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientTakeAllFood, viewer);
                player.getFoodData().setFoodLevel(0);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult FillFood(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            FoodData fs = player.getFoodData();
            if (fs.getFoodLevel() < Tools.MAX_FOOD && player.getHealth() != 0) {
                Log.info(Messages.ServerFillFood, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientFillFood, viewer);
                player.getFoodData().setFoodLevel(Tools.MAX_FOOD);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult FillHearts(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            if (player.getHealth() != Tools.MAX_HEALTH && player.getHealth() != 0) {
                Log.info(Messages.ServerFillAllHearts, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientFillAllHearts, viewer);
                player.setHealth(Tools.MAX_HEALTH);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeHeart(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            if (player.getHealth() > 2) {
                Log.info(Messages.ServerTakeHeart, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientTakeHeart, viewer);
                player.setHealth(player.getHealth() - 2);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult GiveHeart(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            if (player.getHealth() < Tools.MAX_HEALTH && player.getHealth() != 0) {
                Log.info(Messages.ServerGiveHeart, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientGiveHeart, viewer);
                player.setHealth(player.getHealth() + 2);
                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetFire(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            if (player.getRemainingFireTicks() == -20) {
                Log.info(Messages.ServerSetFire, viewer, player.getName().getString());
                SendPlayerMessage(player, Messages.ClientSetFire, viewer);

                player.setSecondsOnFire(5);
                return true;
            }
            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult KillPlayers(PlayerStates states, Player unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            float health = player.getHealth();
            if (health != 0) {
                Log.info(Messages.ServerKill, viewer, player.getName().getString());
                player.getInventory().items.forEach(is -> player.drop(is, false));
                player.getInventory().offhand.forEach(is -> player.drop(is, false));
                player.getInventory().armor.forEach(is -> player.drop(is, false));
                SendPlayerMessage(player, Messages.ClientKill, viewer);
                player.setHealth(0);
                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Unavailable);
    }
}
