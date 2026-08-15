package org.academy.internal.common.ability.builtin.accelerator.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.accelerator.Accelerator;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Deflects nearby projectiles and briefly reduces incoming damage. */
public final class VectorDeviation extends SimpleInstantSkill {
    public static final VectorDeviation INSTANCE = new VectorDeviation();

    private VectorDeviation() {
        super(
                SkillNames.VECTOR_DEVIATION, 2, 7500, List.of(GroundShock.INSTANCE),
                () -> Accelerator.INSTANCE,
                TextureResources.VECTOR_DEVIATION_ICON,
                45, 20,
                GLFW.GLFW_KEY_D, GLFW.GLFW_MOD_ALT,
                75, 100, 0.003F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        double range = 5.0 + 3.0 * proficiency;
        AABB area = player.getBoundingBox().inflate(range);
        List<Projectile> projectiles = player.serverLevel().getEntitiesOfClass(
                Projectile.class,
                area,
                projectile -> projectile.getOwner() != player
        );

        for (Projectile projectile : projectiles) {
            Vec3 motion = projectile.getDeltaMovement();
            Vec3 away = projectile.position().subtract(player.position()).normalize();
            projectile.setDeltaMovement(motion.scale(-1.1 - 0.4 * proficiency).add(away.scale(0.3)));
            projectile.setOwner(player);
            projectile.hurtMarked = true;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                80 + Math.round(60 * proficiency),
                proficiency > 0.6F ? 2 : 1
        ));
        SkillEffects.particles(
                player.serverLevel(), ParticleTypes.ENCHANT,
                player.getBoundingBox().getCenter(), 56, 1.2, 0.1
        );
        return true;
    }
}
