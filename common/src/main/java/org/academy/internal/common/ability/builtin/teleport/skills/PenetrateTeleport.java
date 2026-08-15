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

/** Teleports to the farthest safe point along the look vector, including past thin walls. */
public final class PenetrateTeleport extends SimpleInstantSkill {
    public static final PenetrateTeleport INSTANCE = new PenetrateTeleport();

    private PenetrateTeleport() {
        super(
                SkillNames.PENETRATE_TELEPORT, 2, 6500, List.of(SelfTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.PENETRATE_TELEPORT_ICON,
                60, 46,
                GLFW.GLFW_KEY_P, GLFW.GLFW_MOD_ALT,
                55, 30, 0.004F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.position();
        Vec3 destination = SkillEffects.teleportForward(player, 10.0 + 25.0 * proficiency);
        if (destination == null) return false;
        SkillEffects.particles(player.serverLevel(), ParticleTypes.PORTAL, start, 40, 0.6, 0.1);
        SkillEffects.particles(player.serverLevel(), ParticleTypes.PORTAL, destination, 40, 0.6, 0.1);
        return true;
    }
}
