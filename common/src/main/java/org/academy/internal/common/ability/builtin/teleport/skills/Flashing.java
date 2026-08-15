package org.academy.internal.common.ability.builtin.teleport.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.teleport.Teleport;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Fast short-range blink, preserving the rapid-movement role of the original skill. */
public final class Flashing extends SimpleInstantSkill {
    public static final Flashing INSTANCE = new Flashing();

    private Flashing() {
        super(
                SkillNames.FLASHING, 5, 12500, List.of(ShiftTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.FLASHING_ICON,
                220, 20,
                GLFW.GLFW_KEY_F, GLFW.GLFW_MOD_ALT,
                24, 8, 0.0015F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.position();
        Vec3 destination = SkillEffects.teleportForward(player, 6.0 + 8.0 * proficiency);
        if (destination == null) return false;
        SkillEffects.particles(player.serverLevel(), ParticleTypes.REVERSE_PORTAL, start, 18, 0.35, 0.05);
        SkillEffects.particles(player.serverLevel(), ParticleTypes.REVERSE_PORTAL, destination, 18, 0.35, 0.05);
        return true;
    }
}
