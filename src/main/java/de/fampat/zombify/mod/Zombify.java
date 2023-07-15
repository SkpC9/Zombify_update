package de.fampat.zombify.mod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("zombify")
public class Zombify {
    public Zombify() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void zombifyVillager(ServerLevel world, Villager villager, Zombie zombie) {
        ZombieVillager zombievillager = (ZombieVillager)villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
        zombievillager.finalizeSpawn(world, world.getCurrentDifficultyAt(zombievillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), (CompoundTag)null);
        zombievillager.setVillagerData(villager.getVillagerData());
        zombievillager.setGossips((Tag)villager.getGossips().store(NbtOps.INSTANCE));
        zombievillager.setTradeOffers(villager.getOffers().createTag());
        zombievillager.setVillagerXp(villager.getVillagerXp());
        ForgeEventFactory.onLivingConvert(villager, zombievillager);
        if (!zombie.isSilent()) {
            world.levelEvent((Player)null, 1026, zombie.blockPosition(), 0);
        }

    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity villager = event.getEntity();
        Level world = villager.level();
        if (!world.isClientSide && (world.getDifficulty() == Difficulty.EASY || world.getDifficulty() == Difficulty.NORMAL)) {
            DamageSource source = event.getSource();
            Entity zombie = source.getEntity();
            if (villager instanceof Villager && zombie instanceof Zombie && !villager.isRemoved()) {
                zombifyVillager((ServerLevel)world, (Villager)villager, (Zombie)zombie);
            }
        }
    }
}
