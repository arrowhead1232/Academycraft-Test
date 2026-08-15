package org.academy.internal.common.ability.builtin.meltdowner.skills;

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
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.academy.internal.common.ability.builtin.meltdowner.MeltdownerSkillEffects;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Short-range electron detonation modeled on 1.1.3's EntityMdBall attack. */
public final class ElectronBomb extends SimpleInstantSkill {
    public static final ElectronBomb INSTANCE = new ElectronBomb();

    private ElectronBomb() {
        super(
                SkillNames.ELECTRON_BOMB, 1, 5000, List.of(),
                () -> Meltdowner.INSTANCE,
                TextureResources.ELECTRON_BOMB_ICON,
                15, 45,
                GLFW.GLFW_KEY_E, GLFW.GLFW_MOD_ALT,
                45, 20, 0.005F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        ServerLevel level = player.serverLevel();
        Vec3 destination = SkillEffects.trace(player, 15.0);
        float damage = 6.0F + 6.0F * proficiency;
        AABB area = new AABB(destination, destination).inflate(2.5);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, area, entity -> entity != player && entity.isAlive()
        )) {
            MeltdownerSkillEffects.attack(player, target, damage);
        }
        SkillEffects.particles(level, ParticleTypes.END_ROD, destination, 60, 1.2, 0.16);
        SkillEffects.particles(level, ParticleTypes.SMOKE, destination, 24, 0.8, 0.08);
        return true;
    }
}
