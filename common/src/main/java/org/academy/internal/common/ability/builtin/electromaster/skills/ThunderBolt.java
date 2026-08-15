package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import java.util.Comparator;
import java.util.List;

/** Direct lightning strike with the original skill's secondary area arc. */
public final class ThunderBolt extends SimpleInstantSkill {
    public static final ThunderBolt INSTANCE = new ThunderBolt();

    private ThunderBolt() {
        super(
                SkillNames.THUNDER_BOLT, 4, 10000,
                List.of(ArcGenerate.INSTANCE, CurrentCharging.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.THUNDER_BOLT_ICON,
                150, 70,
                GLFW.GLFW_KEY_B, GLFW.GLFW_MOD_ALT,
                140, 80, 0.005F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 end = SkillEffects.trace(player, 20.0);
        List<LivingEntity> directCandidates = SkillEffects.livingAlongPath(player, start, end, 0.8);
        LivingEntity direct = directCandidates.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .orElse(null);

        float directDamage = 10.0F + 15.0F * proficiency;
        float areaDamage = 6.0F + 9.0F * proficiency;
        if (direct != null) {
            direct.hurt(player.damageSources().playerAttack(player), directDamage);
            direct.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
            end = direct.getBoundingBox().getCenter();
        }

        final LivingEntity directTarget = direct;
        AABB area = new AABB(end, end).inflate(4.0 + 4.0 * proficiency);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, area,
                entity -> entity != player && entity != directTarget && entity.isAlive()
        )) {
            target.hurt(player.damageSources().playerAttack(player), areaDamage);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1));
        }

        var lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(end.x, end.y, end.z);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        SkillEffects.particles(level, ParticleTypes.ELECTRIC_SPARK, end, 48, 1.5, 0.15);
        return true;
    }
}
