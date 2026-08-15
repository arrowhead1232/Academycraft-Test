package org.academy.internal.common.ability.builtin.teleport;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillUse;
import org.academy.internal.common.ability.builtin.teleport.skills.DimFoldingTheorem;
import org.academy.internal.common.ability.builtin.teleport.skills.SpaceFluctuation;

/** Shared dimensional-critical-hit behavior from the 1.1.3 Teleporter tree. */
public final class TeleporterSkillEffects {
    private TeleporterSkillEffects() {
    }

    public static boolean attack(ServerPlayer player, LivingEntity target, float baseDamage) {
        float multiplier = criticalMultiplier(player);
        boolean hurt = target.hurt(
                player.damageSources().playerAttack(player),
                baseDamage * multiplier
        );
        if (hurt && multiplier > 1.0F) {
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.CRIT,
                    target.getBoundingBox().getCenter(), 18, 0.45, 0.08
            );
        }
        return hurt;
    }

    private static float criticalMultiplier(ServerPlayer player) {
        boolean folding = SkillUse.owns(player, DimFoldingTheorem.INSTANCE);
        boolean fluctuation = SkillUse.owns(player, SpaceFluctuation.INSTANCE);
        if (!folding && !fluctuation) return 1.0F;

        float foldingExp = folding ? SkillUse.proficiency(player, DimFoldingTheorem.INSTANCE) : 0.0F;
        float fluctuationExp = fluctuation ? SkillUse.proficiency(player, SpaceFluctuation.INSTANCE) : 0.0F;
        float firstChance = (folding ? 0.10F + 0.10F * foldingExp : 0.0F)
                + (fluctuation ? 0.18F + 0.07F * fluctuationExp : 0.0F);

        float multiplier = 1.0F;
        if (player.getRandom().nextFloat() < firstChance) {
            multiplier = 1.3F;
        } else if (fluctuation && player.getRandom().nextFloat() < 0.10F + 0.05F * fluctuationExp) {
            multiplier = 1.6F;
        } else if (fluctuation && player.getRandom().nextFloat() < 0.01F + 0.02F * fluctuationExp) {
            multiplier = 2.6F;
        }

        if (multiplier > 1.0F) {
            if (folding) SkillUse.grantProficiency(player, DimFoldingTheorem.INSTANCE, 0.0005F);
            if (fluctuation) SkillUse.grantProficiency(player, SpaceFluctuation.INSTANCE, 0.0001F);
        }
        return multiplier;
    }
}
