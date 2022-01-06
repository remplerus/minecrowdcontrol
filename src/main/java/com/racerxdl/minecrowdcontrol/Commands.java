package com.racerxdl.minecrowdcontrol;

import com.racerxdl.minecrowdcontrol.CrowdControl.EffectResult;
import com.racerxdl.minecrowdcontrol.CrowdControl.RequestType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.*;

public class Commands {
    private static final Logger Log = LogManager.getLogger();
    private static boolean enablePlayerMessages = false;
    private static int checkedDifficulty = 0;
    private static final int RANDOM = new Random().nextInt(4);
    private static final World serverWorld = Minecraft.getMinecraft().world;

    public static final Map<String, MinecraftCommand> CommandList = new HashMap<String, MinecraftCommand>() {{
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

    private static final ArrayList<? extends Entity> spawnEntities = new ArrayList(Arrays.asList(
            new EntityCreeper(serverWorld),
            new EntityBlaze(serverWorld),
            new EntityCaveSpider(serverWorld),
            new EntityWitch(serverWorld),
            new EntityZombie(serverWorld),
            new EntityChicken(serverWorld),
            new EntityPig(serverWorld),
            new EntityVillager(serverWorld),
            new EntitySilverfish(serverWorld)
    ));

    private static final ArrayList<? extends Entity> spawnBigEntites = new ArrayList(Arrays.asList(
            new EntitySlime(serverWorld),
            //new EntityWither(serverWorld),
            new EntitySheep(serverWorld),
            new EntityCow(serverWorld),
            //new EntityHorse(serverWorld),
            //new EntityZombieHorse(serverWorld),
            //new EntitySkeletonHorse(serverWorld),
            new EntitySpider(serverWorld)
    ));

    private static final List<Item> spawnItems = new ArrayList(Arrays.asList(
            Items.LEATHER,
            new ItemStack(Blocks.STONE).getItem(),
            new ItemStack(Blocks.LOG).getItem(),
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.DIAMOND,

            new ItemStack(Blocks.CRAFTING_TABLE).getItem(),

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

    private static final List<EnumDifficulty> difficults = new ArrayList<>(Arrays.asList(
            EnumDifficulty.PEACEFUL,
            EnumDifficulty.EASY,
            EnumDifficulty.NORMAL,
            EnumDifficulty.HARD
    ));

    static {
        spawnEntities.forEach((et) -> {
            String entityName = et.getName().toUpperCase().replace(" ", "");
            Log.info("Adding command SPAWN_{}", entityName);
            CommandList.put("SPAWN_" + entityName, (states, u, u1, server, viewer, type) -> SpawnEntity(states, server, viewer, type, et));
        });

        spawnBigEntites.forEach(et -> {
            String entityName = et.getName().toUpperCase().replace(" ", "");
            Log.info("Adding command SPAWN_{}", entityName);
            CommandList.put("SPAWN_" + entityName, (states, u, u1, server, viewer, type) -> SpawnEntity(states, server, viewer, type, et));
        });

        spawnItems.forEach((item) -> {
            String itemName = item.getUnlocalizedName().toUpperCase().replace(" ", "");
            Log.info("Adding command CREATE_{}", itemName);
            CommandList.put("CREATE_" + itemName, (states, player, u1, server, viewer, type) -> SpawnItem(states, player, server, viewer, type, item));
        });

        difficults.forEach((diff) -> {
            String diffName = diff.getDifficultyResourceKey().toUpperCase().replace("OPTION.DIFFICULTY.", "");
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
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        for (EntityPlayer player : players) {
            result |= runnable.run(player);
        }
        return result;
    }

    public static void SendPlayerMessage(EntityPlayer player, String msg, Object... params) {
        if (enablePlayerMessages) {
            SendPlayerSystemMessage(player, msg, params);
        }
    }

    public static void SendPlayerSystemMessage(EntityPlayer player, String msg, Object... params) {
        player.sendStatusMessage(new TextComponentTranslation(MessageFormat.format(msg, params)), false);
    }

    public static void SendSystemMessage(MinecraftServer server, String msg, Object... params) {
        RunOnPlayers(server, (player) -> {
            SendPlayerSystemMessage(player, msg, params);
            return true;
        });
    }

    public static CommandResult SetDifficult(PlayerStates states, EntityPlayer player, MinecraftServer server, String viewer1, RequestType type, EnumDifficulty diff) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);
        
        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop || Objects.requireNonNull(server.getServer()).getDifficulty() == diff) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        Commands.difficults.set(server.getDifficulty().getDifficultyId(), diff);

        Log.info(Messages.ServerSetDifficult, viewer, diff.getDifficultyId());
        SendPlayerMessage(player, Tools.makeTranslation(Messages.ClientSetDifficult), viewer, diff.getDifficultyId());

        return res.SetEffectResult(EffectResult.Success);
    }


    public static CommandResult SpawnItem(PlayerStates states, EntityPlayer p, MinecraftServer server, String viewer1, RequestType type, Item item) {
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
        //TODO
        is.getItem().createEntity(p.world, p, is);
        RunOnPlayers(server, (player -> {
            is.getItem().createEntity(p.world, p, is);

            Log.info(Messages.ServerCreateItem, viewer, player.getName(), item.getUnlocalizedName());
            SendPlayerMessage(player, Messages.ClientCreateItem, viewer, item.getUnlocalizedName());

            return true;
        }));

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult ExplodePlayer(PlayerStates states, EntityPlayer p, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
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
                Log.info(Messages.ServerKill, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientKill, viewer);

                player.getEntityWorld().createExplosion(player, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), 8, true);

                player.getEntityWorld().makeFireworks(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), 0, 0, 0, null);
                player.setHealth(0);
                return true;
            }

            return false;
        }));

        if (result) {
            assert client.player != null;
            client.player.playSound(SoundEvents.ENTITY_ENDERMEN_SCREAM, 6, 0.25f);
            p.getEntityWorld().makeFireworks(p.getPosition().getX(), p.getPosition().getY(), p.getPosition().getZ() + 20, 0, 0, 0, null);
            p.addMovementStat(2, 2, 2); //TODO
        }

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Unavailable);
    }

    public static CommandResult RepairSelectedItem(PlayerStates states, EntityPlayer player, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        int itemIndex = player.inventory.currentItem;
        ItemStack is = player.inventory.mainInventory.get(itemIndex);

        if (is.isEmpty() || is.getRepairCost() != 0 || is.getItemDamage() == 0 || player.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Retry);
        }

        RunOnPlayers(server, (p) -> {
            p.inventory.mainInventory.get(itemIndex).setItemDamage(0);
            return true;
        });

        player.inventory.mainInventory.get(itemIndex).setItemDamage(0);

        assert client.player != null;
        client.player.playSound(SoundEvents.BLOCK_ANVIL_HIT, 1, 1);

        Log.info(Messages.ServerRepairItem, viewer, player.getName(), is.getItem().getUnlocalizedName());
        SendPlayerMessage(player, Messages.ClientRepairItem, viewer, is.getItem().getUnlocalizedName());

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult DropSelectedItem(PlayerStates states, EntityPlayer player, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        int itemIndex = player.inventory.currentItem;
        ItemStack is = player.inventory.mainInventory.get(itemIndex);

        if (is.isEmpty() || player.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Retry);
        }

        RunOnPlayers(server, (p) -> {
            p.dropItem(is, false);
            p.inventory.mainInventory.set(itemIndex, ItemStack.EMPTY);
            return true;
        });

        player.inventory.mainInventory.set(itemIndex, ItemStack.EMPTY);

        assert client.player != null;
        client.player.playSound(SoundEvents.ENTITY_COW_DEATH, 1, 8);

        Log.info(Messages.ServerDropItem, viewer, player.getName(), is.getItem().getUnlocalizedName());
        SendPlayerMessage(player, Messages.ClientDropItem, viewer, is.getItem().getUnlocalizedName());

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult DestroySelectedItem(PlayerStates states, EntityPlayer player, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        int itemIndex = player.inventory.currentItem;
        ItemStack is = player.inventory.mainInventory.get(itemIndex);
        if (is.isEmpty() || player.getHealth() == 0) {
            return res.SetEffectResult(EffectResult.Retry);
        }

        if (is.getCount() > 1) {
            int newCount = is.getCount() - 1;
            is.setCount(newCount);
            RunOnPlayers(server, (p) -> {
                p.inventory.mainInventory.get(itemIndex).setCount(newCount);
                return true;
            });
        } else {
            player.inventory.mainInventory.set(itemIndex, ItemStack.EMPTY);
            RunOnPlayers(server, (p) -> {
                p.inventory.mainInventory.set(itemIndex, ItemStack.EMPTY);
                return true;
            });
        }

        assert client.player != null;
        client.player.playSound(SoundEvents.ENTITY_COW_HURT, 1, 8);

        Log.info(Messages.ServerDestroyItem, viewer, player.getName(), is.getItem().getUnlocalizedName());
        SendPlayerMessage(player, Messages.ClientDestroyItem, viewer, is.getItem().getUnlocalizedName());

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult DrunkMode(PlayerStates states, EntityPlayer player, Minecraft unused, MinecraftServer unused2, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Start) {
            if (states.getDrunkMode()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerDrunkModeStarted, viewer);
            SendPlayerMessage(player, Messages.ClientDrunkModeStarted, viewer);
            PotionEffect eff = new PotionEffect(MobEffects.NAUSEA, 60 * 21, 2); // ~21 ticks = 1 second
            player.addPotionEffect(eff);

            return res
                    .SetNewStates(states.setDrunkMode(true))
                    .SetEffectResult(EffectResult.Success);
        } else if (type == RequestType.Stop) {
            if (!states.getDrunkMode()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerDrunkModeRestored);
            SendPlayerMessage(player, Messages.ClientDrunkModeRestored);

            player.removePotionEffect(MobEffects.NAUSEA);

            return res
                    .SetNewStates(states.setDrunkMode(false))
                    .SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult GottaGoFast(PlayerStates states, EntityPlayer clientPlayer, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);


        boolean result = RunOnPlayers(server, (player) -> {
            if (type == RequestType.Start) {
                if (states.getGottaGoFast()) {
                    return false;
                }
                Log.info(Messages.ServerGottaGoFast, viewer);
                SendPlayerMessage(player, Messages.ClientGottaGoFast, viewer);
                PotionEffect eff = new PotionEffect(MobEffects.SPEED, 60 * 21, 10); // ~21 ticks = 1 second
                player.addPotionEffect(eff);

                return true;
            } else if (type == RequestType.Stop) {
                if (!states.getGottaGoFast()) {
                    return false;
                }

                Log.info(Messages.ServerGottaGoFastRestored);
                SendPlayerMessage(player, Messages.ClientGottaGoFastRestored);

                player.removePotionEffect(MobEffects.SPEED);

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

    public static CommandResult SetRaining(PlayerStates states, EntityPlayer unused, Minecraft client, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        boolean result = RunOnPlayers(server, (player) -> {
            World w = player.world;
            if (type == RequestType.Start) {
                if (w.getWorldInfo().isRaining()) {
                    return false;
                }

                Biome b = w.getBiome(new BlockPos(player.getPosition()));

                if (b.canRain()) {
                    return false;
                }

                Log.info(Messages.ServerMakeItRain, viewer);
                SendPlayerMessage(player, Messages.ClientMakeItRain, viewer);

                Log.info(w.getWorldInfo().isRaining());
                w.getWorldInfo().setRaining(true);
                //TODO
                //w.getServer().getWorldData().overworldData().setClearWeatherTime(0);
                //w.getServer().getWorldData().overworldData().setRainTime(1200);
                //w.getServer().getWorldData().overworldData().setThundering(false);
                return true;
            } else if (type == RequestType.Stop) {
                Log.info(Messages.ServerRainRestored);
                SendPlayerMessage(player, Messages.ClientRainRestored);

                w.getWorldInfo().setRaining(false);

                return true;
            }

            return true;
        });

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetInvertMouse(PlayerStates states, EntityPlayer player, Minecraft client, MinecraftServer unused2, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalizeFully(viewer1);

        if (type == RequestType.Start) {
            if (client.gameSettings.invertMouse) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerInvertMouse, viewer, player.getName());
            SendPlayerMessage(player, Messages.ClientInvertMouse, viewer);
            client.gameSettings.invertMouse = true;
            return res.SetEffectResult(EffectResult.Success);
        } else if (type == RequestType.Stop) {
            if (!client.gameSettings.invertMouse) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerRestoreInvertMouse, player.getName());
            SendPlayerMessage(player, Messages.ClientRestoreInvertMouse);
            client.gameSettings.invertMouse = false;
            return res.SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult SetJumpDisabled(PlayerStates states, EntityPlayer player, Minecraft client, MinecraftServer unused2, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Start) {
            if (res.GetPlayerStates().getJumpDisabled()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerJumpDisabled, viewer, player.getName());
            SendPlayerMessage(player, Messages.ClientJumpDisabled, viewer);
            return res
                    .SetEffectResult(EffectResult.Success)
                    .SetNewStates(res.GetPlayerStates().setJumpDisabled(true));
        } else if (type == RequestType.Stop) {
            if (!res.GetPlayerStates().getJumpDisabled()) {
                return res.SetEffectResult(EffectResult.Retry);
            }

            Log.info(Messages.ServerJumpRestored, player.getName());
            SendPlayerMessage(player, Messages.ClientJumpRestored);
            return res
                    .SetEffectResult(EffectResult.Success)
                    .SetNewStates(res.GetPlayerStates().setJumpDisabled(false));
        }

        return res.SetEffectResult(EffectResult.Success);
    }

    public static CommandResult SetTimeNight(PlayerStates states, EntityPlayer player, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        World world = player.world;
        if (world.getWorldTime() < 13000 || world.getWorldTime() > 23000) {
            Log.info(Messages.ServerSetTimeNight, viewer);
            server.getEntityWorld().setWorldTime(Tools.NIGHT);
            SendPlayerMessage(player, Messages.ClientSetTimeNight, viewer);
            return res.SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Unavailable);
    }

    public static CommandResult SendPlayerToSpawnPoint(PlayerStates states, EntityPlayer entity, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
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
            if (player.getRidingEntity() != null) {
                return false;
            }

            try {
                //TODO
                BlockPos spawnPoint = player.getEntityWorld().getSpawnPoint();// Believe or not, this is spawn point getter
                player.attemptTeleport(spawnPoint.getX(), spawnPoint.getY()+1, spawnPoint.getZ());
            } catch (Exception e) {
                Log.error("Error getting field: " + e);
                return false;
            }

            Log.info(Messages.ServerSendPlayerToSpawnPoint, viewer, player.getName());
            SendPlayerMessage(player, Messages.ClientSendPlayerToSpawnPoint, viewer);

            return true;
        });

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetTimeDay(PlayerStates states, EntityPlayer player, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }
        World world = player.world;

        if (world.getWorldTime() > 6000) {
            Log.info(Messages.ServerSetTimeDay, viewer);
            server.getEntityWorld().setWorldTime(Tools.DAY);
            SendPlayerMessage(player, Messages.ClientSetTimeDay, viewer);
            return res.SetEffectResult(EffectResult.Success);
        }

        return res.SetEffectResult(EffectResult.Unavailable);
    }

    public static CommandResult SpawnEntity(PlayerStates states, MinecraftServer server, String viewer1, RequestType type, Entity entityType) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player) -> {
            if (server.getEntityWorld().getDifficulty() == EnumDifficulty.PEACEFUL) {
                if (checkedDifficulty == 0) {
                    SendPlayerMessage(player, Messages.ClientSpawnPeaceful, viewer);
                    checkedDifficulty = 1;
                }
                return false;
            }

            Vec3d pos = player.getPositionVector();
            if (entityType != null) {//TODO && entityType.isFree(pos.x + RANDOM - 2, pos.y + 1, pos.z + RANDOM -2)) {
                entityType.moveToBlockPosAndAngles(new BlockPos(pos.x + RANDOM - 2, pos.y + 1, RANDOM - 2), 0, 0);

                Log.info(Messages.ServerSpawn, viewer, entityType.getName());
                SendPlayerMessage(player, Messages.ClientSpawn, viewer, entityType.getName());

                player.world.spawnEntity(entityType);
                checkedDifficulty = 0;
            } else {
                return false;
            }

            return true;
        });

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeFood(PlayerStates states, EntityPlayer player, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (p -> {
            FoodStats fs = p.getFoodStats();
            if (fs.getFoodLevel() > 0 && player.getHealth() != 0) {
                Log.info(Messages.ServerTakeFood, viewer, p.getName());
                SendPlayerMessage(p, Messages.ClientTakeFood, viewer);
                fs.setFoodLevel(fs.getFoodLevel() - 2);
                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult GiveFood(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            FoodStats fs = player.getFoodStats();
            if (fs.getFoodLevel() < Tools.MAX_FOOD && player.getHealth() != 0) {
                Log.info(Messages.ServerGiveFood, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientGiveFood, viewer);
                fs.setFoodLevel(fs.getFoodLevel() + 2);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeAllHeartsButHalf(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
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
                Log.info(Messages.ServerTakeAllHeartsButHalf, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientTakeAllHeartsButHalf, viewer);
                player.setHealth(1);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeAllFood(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            FoodStats fs = player.getFoodStats();
            if (fs.getFoodLevel() > 0 && player.getHealth() != 0) {
                Log.info(Messages.ServerTakeAllFood, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientTakeAllFood, viewer);
                player.getFoodStats().setFoodLevel(0);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult FillFood(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            FoodStats fs = player.getFoodStats();
            if (fs.getFoodLevel() < Tools.MAX_FOOD && player.getHealth() != 0) {
                Log.info(Messages.ServerFillFood, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientFillFood, viewer);
                player.getFoodStats().setFoodLevel(Tools.MAX_FOOD);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult FillHearts(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
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
                Log.info(Messages.ServerFillAllHearts, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientFillAllHearts, viewer);
                player.setHealth(Tools.MAX_HEALTH);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult TakeHeart(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
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
                Log.info(Messages.ServerTakeHeart, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientTakeHeart, viewer);
                player.setHealth(player.getHealth() - 2);

                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult GiveHeart(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
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
                Log.info(Messages.ServerGiveHeart, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientGiveHeart, viewer);
                player.setHealth(player.getHealth() + 2);
                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult SetFire(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
        CommandResult res = new CommandResult(states);
        String viewer = WordUtils.capitalize(viewer1);

        if (type == RequestType.Test) {
            return res.SetEffectResult(EffectResult.Success);
        }

        if (type == RequestType.Stop) {
            return res.SetEffectResult(EffectResult.Unavailable);
        }

        boolean result = RunOnPlayers(server, (player -> {
            if (player.canRenderOnFire()) { //TODO
                Log.info(Messages.ServerSetFire, viewer, player.getName());
                SendPlayerMessage(player, Messages.ClientSetFire, viewer);

                player.setFire(5);
                return true;
            }
            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Retry);
    }

    public static CommandResult KillPlayers(PlayerStates states, EntityPlayer unused, Minecraft unused2, MinecraftServer server, String viewer1, RequestType type) {
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
                Log.info(Messages.ServerKill, viewer, player.getName());
                player.inventory.mainInventory.forEach(is -> player.dropItem(is, false));
                player.inventory.offHandInventory.forEach(is -> player.dropItem(is, false));
                player.inventory.armorInventory.forEach(is -> player.dropItem(is, false));
                SendPlayerMessage(player, Messages.ClientKill, viewer);
                player.setHealth(0);
                return true;
            }

            return false;
        }));

        return result ? res.SetEffectResult(EffectResult.Success) : res.SetEffectResult(EffectResult.Unavailable);
    }
}
