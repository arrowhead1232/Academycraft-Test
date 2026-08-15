package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleSustainedSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.academy.internal.common.ability.builtin.meltdowner.MeltdownerSkillEffects;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

/** Sustained auto-targeting electron missiles modeled as short homing rays. */
public final class ElectronMissile extends SimpleSustainedSkill {
    public static final ElectronMissile INSTANCE = new ElectronMissile();

    private ElectronMissile() {
        super(
                SkillNames.ELECTRON_MISSILE, 5, 15000, List.of(JetEngine.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.ELECTRON_MISSILE_ICON,
                210, 35,
                GLFW.GLFW_KEY_M, GLFW.GLFW_MOD_ALT,
                6.0F, 400, 0.0001F
        );
    }

    @Override
    protected Object begin(ServerPlayer player, float proficiency) {
        SkillEffects.particles(
                player.serverLevel(), ParticleTypes.END_ROD,
                player.getBoundingBox().getCenter(), 28, 0.7, 0.05
        );
        return new Object();
    }

    @Override
    protected boolean tick(ServerPlayer player, float proficiency, Object state, int ticks) {
        int timeLimit = 80 + Math.round(120.0F * proficiency);
        if (ticks >= timeLimit) return false;

        double range = 5.0 + 8.0 * proficiency;
        if (ticks % 8 == 0) {
            LivingEntity target = player.serverLevel().getEntitiesOfClass(
                            LivingEntity.class,
                            player.getBoundingBox().inflate(range),
                            entity -> entity != player && entity.isAlive() && !player.isAlliedTo(entity)
                    ).stream()
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                    .orElse(null);
            if (target != null) {
                MeltdownerSkillEffects.attack(player, target, 10.0F + 8.0F * proficiency);
                spawnRay(player, target.getBoundingBox().getCenter());
            }
        }

        if (ticks % 3 == 0) {
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.END_ROD,
                    player.getBoundingBox().getCenter(), 3, 0.8, 0.025
            );
        }
        return true;
    }

    private static void spawnRay(ServerPlayer player, Vec3 end) {
        Vec3 start = player.getEyePosition();
        Vec3 delta = end.subtract(start);
        for (int i = 1; i <= 12; i++) {
            Vec3 point = start.add(delta.scale(i / 12.0));
            SkillEffects.particles(player.serverLevel(), ParticleTypes.END_ROD, point, 1, 0.02, 0.0);
        }
    }
}
