package org.academy.internal.common.ability.builtin.teleport.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.teleport.Teleport;
import org.academy.internal.common.world.entity.player.PlayerSyncData;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Persistent same-dimension waypoint teleport; sneak-use replaces the waypoint. */
public final class LocationTeleport extends SimpleInstantSkill {
    private static final String KEY_SET = "academy_location_teleport_set";
    private static final String KEY_DIMENSION = "academy_location_teleport_dimension";
    private static final String KEY_X = "academy_location_teleport_x";
    private static final String KEY_Y = "academy_location_teleport_y";
    private static final String KEY_Z = "academy_location_teleport_z";

    public static final LocationTeleport INSTANCE = new LocationTeleport();

    private LocationTeleport() {
        super(
                SkillNames.LOCATION_TELEPORT, 3, 10000,
                List.of(PenetrateTeleport.INSTANCE, MarkTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.LOCATION_TELEPORT_ICON,
                118, 50,
                GLFW.GLFW_KEY_H, GLFW.GLFW_MOD_ALT,
                220, 30, 0.015F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        CompoundTag data = player.getEntityData().get(PlayerSyncData.DATA);
        if (!data.getBoolean(KEY_SET) || player.isShiftKeyDown()) {
            CompoundTag updatedData = data.copy();
            updatedData.putBoolean(KEY_SET, true);
            updatedData.putString(KEY_DIMENSION, player.level().dimension().location().toString());
            updatedData.putDouble(KEY_X, player.getX());
            updatedData.putDouble(KEY_Y, player.getY());
            updatedData.putDouble(KEY_Z, player.getZ());
            player.getEntityData().set(PlayerSyncData.DATA, updatedData);
            player.displayClientMessage(Component.literal("Location Teleport waypoint saved."), true);
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.REVERSE_PORTAL,
                    player.position(), 32, 0.45, 0.05
            );
            return true;
        }

        String dimension = data.getString(KEY_DIMENSION);
        if (!dimension.equals(player.level().dimension().location().toString())) {
            player.displayClientMessage(
                    Component.literal("The saved waypoint is in another dimension."), true
            );
            return false;
        }

        Vec3 destination = new Vec3(
                data.getDouble(KEY_X), data.getDouble(KEY_Y), data.getDouble(KEY_Z)
        );
        BlockPos blockPos = BlockPos.containing(destination);
        if (!player.serverLevel().hasChunkAt(blockPos)) {
            player.displayClientMessage(Component.literal("The saved waypoint is not loaded."), true);
            return false;
        }
        AABB destinationBox = player.getBoundingBox().move(destination.subtract(player.position()));
        if (!player.serverLevel().noCollision(player, destinationBox)) {
            player.displayClientMessage(Component.literal("The saved waypoint is obstructed."), true);
            return false;
        }

        Vec3 start = player.position();
        if (player.isPassenger()) player.stopRiding();
        player.teleportTo(destination.x, destination.y, destination.z);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        SkillEffects.particles(player.serverLevel(), ParticleTypes.PORTAL, start, 60, 0.7, 0.12);
        SkillEffects.particles(player.serverLevel(), ParticleTypes.PORTAL, destination, 60, 0.7, 0.12);
        return true;
    }
}
