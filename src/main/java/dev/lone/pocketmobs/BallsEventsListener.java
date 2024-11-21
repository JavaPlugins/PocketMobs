package dev.lone.pocketmobs;

import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.pocketmobs.data.Ball;
import dev.lone.pocketmobs.data.BallEffectType;
import dev.lone.pocketmobs.utils.*;
import dev.lone.pocketmobs.utils.Raycast;
import fr.mrmicky.fastparticles.ParticleType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class BallsEventsListener implements Listener
{
    private static final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

    //drop entity id, item
    HashMap<Integer, Item> balls = new HashMap<>();
    //drop entity id, player
    HashMap<Integer, Player> ballsByPlayer = new HashMap<>();
    //mob UUID, Item drop
    static HashMap<UUID, Item> ballsByMob = new HashMap<>();

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        ItemStack item = e.getItem();
        if (!Ball.is(item))
            return;

        e.setCancelled(true);

        if (!Settings.worlds.contains(e.getPlayer().getLocation().getWorld().getName()))
            return;

        // Handle throwing the ball to catch a mob
        if (!Ball.hasMob(item))
        {
            if (Ball.getUsages(item) <= 0)
            {
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ITEM_BREAK.playSound(), 1, 1);
                showMessageFeedback(e.getPlayer(), "ball-usages-over", Settings.lang.getColored("ball-usages-over"));
                InvUtil.decrementAmount(item);
                return;
            }

            ItemStack thrown = e.getItem().clone();
            thrown.setAmount(1);
            //e.getPlayer().getInventory().remove(thrown);
            InvUtil.decrementAmount(item);

            NBTItem nbtItem = new NBTItem(thrown);
            nbtItem.setInteger("PBAntiStackRandom", Utils.getRandomInt(1, 99999));
            thrown.setItemMeta(nbtItem.getItem().getItemMeta());

            Item drop = e.getPlayer().getWorld().dropItem(e.getPlayer().getEyeLocation(), thrown);
            drop.setCustomNameVisible(false);
            drop.setMetadata("PBIsBall", new FixedMetadataValue(Main.inst, true));

            balls.put(drop.getEntityId(), drop);
            ballsByPlayer.put(drop.getEntityId(), e.getPlayer());

            Arrow ball = e.getPlayer().launchProjectile(Arrow.class);
            //ball.setDamage(0);
            ball.setMetadata("PBCatch", new FixedMetadataValue(Main.inst, drop.getEntityId()));
            ball.setShooter(e.getPlayer());
            Utils.hideEntity(ball);

            drop.setVelocity(ball.getVelocity());
        }
        else
        {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
            {
                throwBallFreeMob(e, e.getPlayer(), e.getItem());
            }
        }
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        if (e.isCancelled())
            return;

        if (e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (!Ball.is(e.getPlayer().getItemInHand()))
            return;

        if (!Settings.worlds.contains(e.getPlayer().getLocation().getWorld().getName()))
        {
            e.setCancelled(true);
            return;
        }

        throwBallFreeMob(e, e.getPlayer(), e.getPlayer().getItemInHand());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void catchMobOnHit(EntityDamageByEntityEvent e)
    {
        Entity mobEntity = e.getEntity();
        Entity damager = e.getDamager();

        if (!damager.hasMetadata("PBCatch"))
            return;

        damager.remove();

        int PBCatch = damager.getMetadata("PBCatch").get(0).asInt();
        Item ballEntity = balls.get(PBCatch);

        //not your claim
        if (e.isCancelled())
        {
            ballEntity.teleport(ballsByPlayer.get(PBCatch));
            ballEntity.setVelocity(new Vector(0, 0, 0));
            return;
        }

        e.setCancelled(true);

        Ball settings = Main.inst.ballsManager.byItemStack(ballEntity.getItemStack());

        Player player = ballsByPlayer.get(PBCatch);
        if (
                e.getEntityType() != EntityType.UNKNOWN &&
                        e.getEntityType() != EntityType.PLAYER &&
                        settings.isCatchableMob(e.getEntity()) &&
                        (
                                e.getEntity() instanceof LivingEntity &&
                                        ((LivingEntity) e.getEntity()).hasAI()
                        )
        )
        {
            ItemStack ballItemStack = ballEntity.getItemStack();

            if (!settings.unlimitedUsages)
            {
                ballItemStack = Ball.reduceUsages(ballItemStack, 1);
                ballEntity.setItemStack(ballItemStack);
            }

            if (!settings.rollDice())
            {
                if (Ball.getUsages(ballItemStack) > 0)
                {
                    ballEntity.teleport(player);
                    ballEntity.setVelocity(new Vector(0, 0, 0));

                    ActionBar.send(player, Settings.lang.getColored("failed-to-catch")
                            .replace("{chance}", settings.catchSuccessPercentage + "")
                    );
                }
                else
                {
                    ballEntity.remove();
                    ActionBar.send(player, Settings.lang.getColored("failed-to-catch-no-usages")
                            .replace("{chance}", settings.catchSuccessPercentage + "")
                    );
                }
            }
            else
            {
                mobEntity.remove();

                ballEntity.setItemStack(Ball.catchMob(ballItemStack, mobEntity));

                ballEntity.teleport(mobEntity.getLocation());
                ballEntity.setVelocity(new Vector(0, 0.4f, 0));
                ballEntity.setGlowing(true);

                settings.playEffect(BallEffectType.CATCH, e.getEntity().getLocation());

                showMessageFeedback(player, ballEntity, "catch-success", Settings.lang.getColored("caugth-successfully"));

                if (Settings.returnToInvCatch)
                {
                    ballEntity.setPickupDelay(0);
                    ballEntity.teleport(player.getLocation());
                    ballEntity.setVelocity(new Vector(0, 0, 0));
                }
            }
        }
        else
        {
            ballEntity.teleport(ballsByPlayer.get(PBCatch));
            ballEntity.setVelocity(new Vector(0, 0, 0));

            ActionBar.send(player, Settings.lang.getColored("cant-catch-with-this-ball"));
        }

        ballsByPlayer.remove(PBCatch);
        balls.remove(PBCatch);
    }

    @EventHandler
    private void spawnMobOnHit(ProjectileHitEvent e)
    {
        Entity projectile = e.getEntity();

        if (!projectile.hasMetadata("PBSpawn"))
            return;

        int PBSpawn = projectile.getMetadata("PBSpawn").get(0).asInt();

        Item drop = balls.get(PBSpawn);
        Player player = ballsByPlayer.get(PBSpawn);

        projectile.remove();

        InvUtil.decrementAmountMainHand(player);


        if (!LocationUtil.canBreak(projectile.getLocation().getBlock(), player))
        {
            drop.teleport(player);
            drop.setVelocity(new Vector(0, 0, 0));

            balls.remove(PBSpawn);
            ballsByPlayer.remove(PBSpawn);
            return;
        }

        Raycast ray = new Raycast(player, 30);
        Location finalLoc = projectile.getLocation();
        if (ray.compute(Raycast.RaycastType.BLOCK) && ray.hasHitBlock())
        {
            Block finalBlockLoc = ray.getHitBlock();
            if (ray.getHitFace() != null)
                finalBlockLoc = finalBlockLoc.getRelative(ray.getHitFace());

            finalLoc = Utils.getBlockCenter(finalBlockLoc);

            if (finalBlockLoc.isSolid())
            {
                // Find air block
                for (BlockFace face : FACES)
                {
                    if (!finalBlockLoc.getRelative(face).getType().isSolid())
                    {
                        finalLoc = Utils.getBlockCenter(finalBlockLoc.getRelative(face));
                        break;
                    }
                }
            }
        }

        Ball.freeMob(drop, finalLoc);

        Ball ballSettings = Main.inst.ballsManager.byItemStack(drop.getItemStack());
        ballSettings.playEffect(BallEffectType.FREE, e.getEntity().getLocation());

        if (Ball.getUsages(drop.getItemStack()) <= 0)
        {
            player.playSound(player.getLocation(), Sound.ITEM_BREAK.playSound(), 1, 1);
            showMessageFeedback(player, "ball-usages-over", Settings.lang.getColored("ball-usages-over"));
            drop.remove();
        }

        balls.remove(PBSpawn);
        ballsByPlayer.remove(PBSpawn);
    }

    @EventHandler
    private void missedMobCatch(ProjectileHitEvent e)
    {
        if (e.getHitEntity() != null)
            return;

        Entity projectile = e.getEntity();

        if (!projectile.hasMetadata("PBCatch"))
            return;

        int PBCatch = projectile.getMetadata("PBCatch").get(0).asInt();

        Item ballEntity = (Item) balls.get(PBCatch);
        ballEntity.setVelocity(new Vector(0, 0.1f, 0));
        ballEntity.teleport(projectile.getLocation());

        projectile.remove();

        Ball ballSettings = Main.inst.ballsManager.byItemStack(ballEntity.getItemStack());

        if (!ballSettings.unlimitedUsages)
        {
            if (Settings.reduceUsagesOnMiss)
                ballEntity.setItemStack(Ball.reduceUsages(ballEntity.getItemStack(), 1));
        }
        ballSettings.playEffect(BallEffectType.MISSED, e.getEntity().getLocation());

        if (Ball.getUsages(ballEntity.getItemStack()) > 0)
        {
            //ActionBar.send(droppedPokeyballs_byPlayer.get(PBCatch), Main.lang.getColored("missed-catch"));
            ballEntity.setCustomNameVisible(true);
            ballEntity.setCustomName(Settings.lang.getColored("missed-catch"));
        }
        else
        {
            Player player = ballsByPlayer.get(PBCatch);
            showMessageFeedback(player, "ball-usages-over", Settings.lang.getColored("missed-catch-no-usages"));
            ballEntity.remove();
        }

        balls.remove(PBCatch);
        ballsByPlayer.remove(PBCatch);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void spawnEvent(CreatureSpawnEvent e)
    {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;

        if (!ballsByMob.containsKey(e.getEntity().getUniqueId()))
            return;

        Item drop = ballsByMob.get(e.getEntity().getUniqueId());
        if (!e.isCancelled()) // If the mob has been spawned then I have to eliminate the ball from the ground and spawn an empty one
        {
            drop.remove();

            if (Ball.getUsages(drop.getItemStack()) <= 0)
            {
                ParticleType.of("ITEM_CRACK").spawn(e.getEntity().getWorld(), e.getEntity().getLocation(), 1, drop.getItemStack());
                return;
            }
            else
            {
                Item newPoke = drop.getWorld().dropItem(drop.getLocation(), Ball.removeMob(drop.getItemStack()));
                newPoke.setCustomNameVisible(false);
                if (Settings.returnToInvFree)
                {
                    newPoke.setPickupDelay(0);
                    newPoke.teleport(ballsByPlayer.get(drop.getEntityId()));
                    newPoke.setVelocity(new Vector(0, 0, 0));
                }

            }

        }
        else // Otherwise I don't eliminate the ball otherwise the player would lose the ball without having freed the mob
        {
            drop.setVelocity(new Vector(0, 0, 0));
            drop.teleport(e.getLocation());
            Ball ballSettings = Main.inst.ballsManager.byItemStack(drop.getItemStack());
            ballSettings.playEffect(BallEffectType.NOT_SPAWNED, e.getLocation());

            ballsByPlayer.get(drop.getEntityId()).sendMessage(Settings.lang.getColored("failed-to-spawn-mob"));
        }

        ballsByMob.remove(e.getEntity().getUniqueId());
        ballsByPlayer.remove(e.getEntity().getEntityId());
    }

    @EventHandler
    private void onPrepareItemCraft(PrepareItemCraftEvent e)
    {
        if (e.isRepair())
            return;

        if (e.getRecipe() == null)
            return;

        if (!Ball.is(e.getRecipe().getResult()))
            return;

        Ball ballConfig = Main.inst.ballsManager.byItemStack(e.getRecipe().getResult());
        if (!Settings.worlds.contains(e.getView().getPlayer().getLocation().getWorld().getName()) || !e.getView().getPlayer().hasPermission("pocketmob.user.craft." + ballConfig.name))
        {
            e.getInventory().setResult(new ItemStack(Material.AIR, 1));
            e.getView().getPlayer().sendMessage(Settings.lang.getColored("no-craft-permission"));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onHopperPickupItem(InventoryPickupItemEvent e)
    {
        //fixes duplication of ball https://github.com/PluginBugs/Issues-PocketMobs/issues/5
        if (e.getItem().hasMetadata("PBIsBall"))
        {
            //bounce the prev item away, to avoid making it sucked into the hopper. This is a total hack.
            e.getItem().setVelocity(new Vector(0.2, 0.2, 0.2));
            e.setCancelled(true);
        }
    }

    private void throwBallFreeMob(Cancellable e, Player player, ItemStack ball)
    {
        e.setCancelled(true);

        if (!Ball.hasMob(ball))
            return;

        ItemStack thrown = ball.clone();
        thrown.setAmount(1);

        //https://github.com/PluginBugs/Issues-PocketMobs/issues/3
        InvUtil.decrementAmountMainHand(player);

        NBTItem nbtItem = new NBTItem(thrown);
        nbtItem.setInteger("PBAntiStackRandom", Utils.getRandomInt(1, 99999));
        thrown.setItemMeta(nbtItem.getItem().getItemMeta());


        Item drop = player.getWorld().dropItem(player.getEyeLocation(), thrown);
        drop.setCustomNameVisible(false);
        drop.setMetadata("PBIsBall", new FixedMetadataValue(Main.inst, true));

        balls.put(drop.getEntityId(), drop);
        ballsByPlayer.put(drop.getEntityId(), player);

        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setMetadata("PBSpawn", new FixedMetadataValue(Main.inst, drop.getEntityId()));
        arrow.setShooter(player);
        Utils.hideEntity(arrow);

        drop.setVelocity(arrow.getVelocity());
    }

    void showMessageFeedback(Player player, String event, String message)
    {
        if (Settings.config.getBoolean("indicators." + event + ".hologram.enabled"))
        {
            HologramUtil.send(player, message, Settings.config.getInt("indicators." + event + ".hologram.duration") * 20);
        }

        if (Settings.config.getBoolean("indicators." + event + ".actionbar.enabled"))
        {
            ActionBar.send(player, message);
        }

        if (Settings.config.getBoolean("indicators." + event + ".chat.enabled"))
        {
            player.sendMessage(message);
        }
    }

    void showMessageFeedback(Player player, Item drop, String event, String message)
    {
        if (Settings.config.getBoolean("indicators." + event + ".hologram.enabled"))
        {
            drop.setCustomNameVisible(true);
            drop.setCustomName(message);
        }

        if (Settings.config.getBoolean("indicators." + event + ".actionbar.enabled"))
        {
            ActionBar.send(player, message);
        }

        if (Settings.config.getBoolean("indicators." + event + ".chat.enabled"))
        {
            player.sendMessage(message);
        }
    }
}
