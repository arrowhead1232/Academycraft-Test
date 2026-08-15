package org.academy.internal.common.ability.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.academy.api.common.ability.AbilityCategory;
import org.academy.api.common.ability.Skill;
import org.academy.api.server.ability.AbilitySystemServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side ownership, CP, cooldown, and proficiency helpers for skills. */
public final class SkillUse {
    private static final Map<CooldownKey, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static volatile MinecraftServer boundServer;

    private SkillUse() {
    }

    public static synchronized void bindServer(MinecraftServer server) {
        if (boundServer != server) {
            COOLDOWNS.clear();
            boundServer = server;
        }
    }

    public static boolean owns(ServerPlayer player, Skill skill) {
        UUID uuid = player.getUUID();
        try {
            if (!AbilitySystemServer.getPlayerSkills(uuid).contains(skill.name)) return false;
            AbilityCategory category = AbilitySystemServer.getPlayerAbilityCategory(uuid);
            return category != null && category.skillList.contains(skill);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean isReady(ServerPlayer player, Skill skill) {
        long readyAt = COOLDOWNS.getOrDefault(new CooldownKey(player.getUUID(), skill.name), 0L);
        return player.serverLevel().getGameTime() >= readyAt;
    }

    public static boolean canActivate(ServerPlayer player, Skill skill, float computingPowerCost) {
        return owns(player, skill)
                && isReady(player, skill)
                && AbilitySystemServer.getPlayerComputingPower(player.getUUID()) >= computingPowerCost;
    }

    public static boolean consumeComputingPower(ServerPlayer player, float amount) {
        UUID uuid = player.getUUID();
        float current = AbilitySystemServer.getPlayerComputingPower(uuid);
        if (current < amount) return false;
        AbilitySystemServer.setPlayerComputingPower(uuid, current - amount);
        return true;
    }

    public static void setCooldown(ServerPlayer player, Skill skill, int cooldownTicks) {
        COOLDOWNS.put(
                new CooldownKey(player.getUUID(), skill.name),
                player.serverLevel().getGameTime() + Math.max(0, cooldownTicks)
        );
    }

    public static float proficiency(ServerPlayer player, Skill skill) {
        return AbilitySystemServer.getPlayerSkillExp(player.getUUID(), skill.name);
    }

    public static void grantProficiency(ServerPlayer player, Skill skill, float amount) {
        float current = proficiency(player, skill);
        AbilitySystemServer.setPlayerSkillExp(
                player.getUUID(), skill.name, Math.min(1.0F, current + Math.max(0.0F, amount))
        );
    }

    public static void complete(
            ServerPlayer player, Skill skill, float computingPowerCost,
            int cooldownTicks, float proficiencyGain
    ) {
        consumeComputingPower(player, computingPowerCost);
        grantProficiency(player, skill, proficiencyGain);
        setCooldown(player, skill, cooldownTicks);
    }

    private record CooldownKey(UUID player, String skill) {
    }
}
