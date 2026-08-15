package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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

/** Electron-stream dash retaining the movement/ram role of the 1.1.3 skill. */
public final class JetEngine extends SimpleInstantSkill {
    public static final JetEngine INSTANCE = new JetEngine();

    private JetEngine() {
        super(
                SkillNames.JET_ENGINE, 4, 11000, List.of(SingleHighSpeedElectronBeam.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.JET_ENGINE_ICON,
                170, 32,
                GLFW.GLFW_KEY_SPACE, GLFW.GLFW_MOD_ALT,
                75, 30, 0.003F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.position();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 end = start.add(direction.scale(6.0 + 4.0 * proficiency));
        for (var target : SkillEffects.livingAlongPath(player, start, end, 1.0)) {
            MeltdownerSkillEffects.attack(player, target, 7.0F + 7.0F * proficiency);
        }

        player.setDeltaMovement(direction.scale(1.5 + 1.0 * proficiency).add(0, 0.12, 0));
        player.resetFallDistance();
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        SkillEffects.particles(player.serverLevel(), ParticleTypes.FLAME, start, 32, 0.5, 0.12);
        return true;
    }
}
