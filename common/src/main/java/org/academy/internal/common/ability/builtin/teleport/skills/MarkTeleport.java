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

/** Long-range aimed teleport corresponding to 1.1.3's Mark Teleport. */
public final class MarkTeleport extends SimpleInstantSkill {
    public static final MarkTeleport INSTANCE = new MarkTeleport();

    private MarkTeleport() {
        super(
                SkillNames.MARK_TELEPORT, 2, 7000, List.of(SelfTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.MARK_TELEPORT_ICON,
                70, 16,
                GLFW.GLFW_KEY_M, GLFW.GLFW_MOD_ALT,
                150, 30, 0.004F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.position();
        Vec3 destination = SkillEffects.teleportForward(player, 25.0 + 35.0 * proficiency);
        if (destination == null || destination.distanceToSqr(start) < 9.0) return false;
        SkillEffects.particles(player.serverLevel(), ParticleTypes.PORTAL, start, 52, 0.65, 0.12);
        SkillEffects.particles(player.serverLevel(), ParticleTypes.PORTAL, destination, 52, 0.65, 0.12);
        return true;
    }
}
