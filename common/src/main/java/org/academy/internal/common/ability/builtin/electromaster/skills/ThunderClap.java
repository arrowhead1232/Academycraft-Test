package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.electromaster.Electromaster;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** High-cost area lightning strike inspired by 1.1.3's charged Thunder Clap. */
public final class ThunderClap extends SimpleInstantSkill {
    public static final ThunderClap INSTANCE = new ThunderClap();

    private ThunderClap() {
        super(
                SkillNames.THUNDER_CLAP, 5, 15000, List.of(ThunderBolt.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.THUNDER_CLAP_ICON,
                200, 70,
                GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_ALT,
                320, 240, 0.003F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        ServerLevel level = player.serverLevel();
        Vec3 targetPoint = SkillEffects.trace(player, 40.0);
        double radius = 8.0 + 8.0 * proficiency;
        float damage = 12.0F + 12.0F * proficiency;
        AABB area = new AABB(targetPoint, targetPoint).inflate(radius);

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive()
        )) {
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                Vec3 push = target.position().subtract(targetPoint);
                if (push.lengthSqr() > 0.01) {
                    target.push(push.x * 0.12, 0.5, push.z * 0.12);
                    target.hurtMarked = true;
                }
            }
        }

        var lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(targetPoint.x, targetPoint.y, targetPoint.z);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        SkillEffects.particles(level, ParticleTypes.ELECTRIC_SPARK, targetPoint, 120, radius * 0.35, 0.3);
        return true;
    }
}
