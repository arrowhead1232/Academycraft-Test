package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.academy.internal.common.ability.builtin.meltdowner.MeltdownerSkillEffects;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Five-beam spread attack based on 1.1.3's Scatter Bomb. */
public final class ScatterBomb extends SimpleInstantSkill {
    public static final ScatterBomb INSTANCE = new ScatterBomb();

    private ScatterBomb() {
        super(
                SkillNames.SCATTER_BOMB, 3, 10000, List.of(ElectronBomb.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.SCATTER_BOMB_ICON,
                70, 50,
                GLFW.GLFW_KEY_S, GLFW.GLFW_MOD_ALT,
                130, 60, 0.004F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        float damage = 4.0F + 6.0F * proficiency;
        float[] angles = {-0.24F, -0.12F, 0.0F, 0.12F, 0.24F};
        for (float angle : angles) {
            Vec3 direction = look.yRot(angle);
            Vec3 end = SkillEffects.trace(player, direction, 18.0 + 8.0 * proficiency);
            for (var target : SkillEffects.livingAlongPath(player, start, end, 0.45)) {
                MeltdownerSkillEffects.attack(player, target, damage);
            }
            SkillEffects.particles(player.serverLevel(), ParticleTypes.END_ROD, end, 12, 0.25, 0.05);
        }
        return true;
    }
}
