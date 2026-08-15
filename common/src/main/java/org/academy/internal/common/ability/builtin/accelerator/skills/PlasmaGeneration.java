package org.academy.internal.common.ability.builtin.accelerator.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.accelerator.Accelerator;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** High-level plasma cannon approximation for the previously empty tree entry. */
public final class PlasmaGeneration extends SimpleInstantSkill {
    public static final PlasmaGeneration INSTANCE = new PlasmaGeneration();

    private PlasmaGeneration() {
        super(
                SkillNames.PLASMA_GENERATION, 5, 15000,
                List.of(VectorReflection.INSTANCE, StormWing.INSTANCE),
                () -> Accelerator.INSTANCE,
                TextureResources.PLASMA_GENERATION_ICON,
                200, 20,
                GLFW.GLFW_KEY_X, GLFW.GLFW_MOD_ALT,
                320, 240, 0.003F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 end = SkillEffects.trace(player, 28.0 + 12.0 * proficiency);
        float beamDamage = 16.0F + 20.0F * proficiency;
        SkillEffects.damageAlongPath(player, start, end, 1.2, beamDamage);

        double radius = 3.0 + 2.0 * proficiency;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(end, end).inflate(radius),
                entity -> entity != player && entity.isAlive()
        )) {
            if (target.hurt(player.damageSources().playerAttack(player), beamDamage * 0.65F)) {
                target.setSecondsOnFire(3 + Math.round(3 * proficiency));
                Vec3 push = target.position().subtract(end);
                if (push.lengthSqr() > 0.01) {
                    target.push(push.x * 0.18, 0.45, push.z * 0.18);
                    target.hurtMarked = true;
                }
            }
        }

        SkillEffects.particles(level, ParticleTypes.FLAME, end, 120, radius * 0.45, 0.22);
        SkillEffects.particles(level, ParticleTypes.LARGE_SMOKE, end, 42, radius * 0.35, 0.08);
        return true;
    }
}
