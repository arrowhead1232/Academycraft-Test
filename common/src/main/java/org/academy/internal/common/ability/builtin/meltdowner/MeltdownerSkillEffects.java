package org.academy.internal.common.ability.builtin.meltdowner;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillUse;
import org.academy.internal.common.ability.builtin.meltdowner.skills.RadiationIntensify;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Shared irradiation mark and follow-up damage behavior for Meltdowner attacks. */
public final class MeltdownerSkillEffects {
    private static final long MARK_DURATION_TICKS = 200L;
    private static final Map<UUID, Long> MARKED_UNTIL = new ConcurrentHashMap<>();

    private MeltdownerSkillEffects() {
    }

    public static boolean attack(ServerPlayer player, LivingEntity target, float baseDamage) {
        long gameTime = player.serverLevel().getGameTime();
        float damage = isMarked(target, gameTime) ? baseDamage * 1.5F : baseDamage;
        boolean hurt = target.hurt(player.damageSources().playerAttack(player), damage);
        if (hurt && SkillUse.owns(player, RadiationIntensify.INSTANCE)) {
            mark(target, gameTime);
            SkillUse.grantProficiency(player, RadiationIntensify.INSTANCE, 0.0003F);
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.END_ROD,
                    target.getBoundingBox().getCenter(), 8, 0.35, 0.035
            );
        }
        return hurt;
    }

    public static boolean isMarked(LivingEntity target, long gameTime) {
        Long markedUntil = MARKED_UNTIL.get(target.getUUID());
        if (markedUntil == null) return false;
        if (markedUntil <= gameTime) {
            MARKED_UNTIL.remove(target.getUUID(), markedUntil);
            return false;
        }
        return true;
    }

    private static void mark(LivingEntity target, long gameTime) {
        MARKED_UNTIL.put(target.getUUID(), gameTime + MARK_DURATION_TICKS);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false, true));
    }
}
