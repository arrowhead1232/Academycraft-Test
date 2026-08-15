package org.academy.internal.common.ability.builtin.accelerator.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.api.common.util.LevelUtil;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.accelerator.Accelerator;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Forward ground shockwave with gamerule-respecting terrain damage. */
public final class GroundShock extends SimpleInstantSkill {
    public static final GroundShock INSTANCE = new GroundShock();

    private GroundShock() {
        super(
                SkillNames.GROUND_SHOCK, 1, 5000, List.of(VectorReflection.INSTANCE),
                () -> Accelerator.INSTANCE,
                TextureResources.GROUND_SHOCK_ICON,
                45, 105,
                GLFW.GLFW_KEY_H, GLFW.GLFW_MOD_ALT,
                100, 60, 0.004F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        if (!player.onGround()) return false;
        Vec3 direction = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
        if (direction.lengthSqr() < 0.01) return false;

        Vec3 start = player.position().add(0, -0.6, 0);
        Vec3 end = start.add(direction.scale(8.0 + 10.0 * proficiency));
        if (player.getAbilities().mayBuild
                && player.serverLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            LevelUtil.destroyBlocksAlongPath(
                    player.serverLevel(), start, end,
                    0.65F + 0.35F * proficiency,
                    1, false, true, true, false
            );
        }

        float damage = 4.0F + 4.0F * proficiency;
        for (LivingEntity target : SkillEffects.livingAlongPath(player, start, end, 1.25)) {
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                target.push(direction.x * (0.8 + proficiency), 0.65 + 0.25 * proficiency,
                        direction.z * (0.8 + proficiency));
                target.hurtMarked = true;
            }
        }
        SkillEffects.particles(player.serverLevel(), ParticleTypes.CAMPFIRE_COSY_SMOKE, end, 30, 1.0, 0.08);
        return true;
    }
}
