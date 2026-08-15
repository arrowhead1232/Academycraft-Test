package org.academy.internal.common.ability.builtin.accelerator.skills;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.academy.AcademyCraftClient;
import org.academy.AcademyCraftConfig;
import org.academy.AcademyCraftServer;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.api.client.config.KeyBindingConfig;
import org.academy.api.client.input.InputSystem;
import org.academy.api.client.network.NetworkManagerClient;
import org.academy.api.client.resource.TextureResources;
import org.academy.api.common.ability.Skill;
import org.academy.api.common.config.IConfigAction;
import org.academy.api.common.network.PacketTarget;
import org.academy.api.common.network.SubscribePacket;
import org.academy.api.common.network.packet.C2SPacket;
import org.academy.api.common.network.packet.EmptyPacket;
import org.academy.api.common.vanilla.ThreadType;
import org.academy.internal.client.gui.screen.AbilityDeveloperScreen;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillUse;
import org.academy.internal.common.ability.builtin.accelerator.Accelerator;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BloodflowReverse extends Skill {
    public static final Skill INSTANCE = new BloodflowReverse();

    private BloodflowReverse() {
        super(SkillNames.BLOODFLOW_REVERSE, 2, List.of(VectorReflection.INSTANCE));
    }

    @Override
    public void initClient() {
        AcademyCraftConfig.registerConfigActions(INSTANCE.name, Client.Config.Action.INSTANCE);
        Client.CONFIG = AcademyCraftClient.CLIENT_CONFIG.getConfig(INSTANCE.name);
        if (Client.CONFIG == null) {
            Client.CONFIG = new Client.Config();
            AcademyCraftClient.CLIENT_CONFIG.setConfig(INSTANCE.name, Client.CONFIG);
        }

        InputSystem.addKeyBinding(Client.KEY_NAME, Client.CONFIG.getKeyBinding(Client.KEY_NAME,
                        new InputSystem.InputPair(
                                InputSystem.InputType.KEYBOARD,
                                new InputSystem.KeyInfo(
                                        new LinkedHashSet<>(Set.of(GLFW.GLFW_KEY_R)),
                                        GLFW.GLFW_RELEASE,
                                        new LinkedHashSet<>(
                                                Set.of(GLFW.GLFW_MOD_ALT)
                                        )
                                )
                        )
                ), Client::reverseBloodflow
        );
    }

    @Override
    public void initServer(MinecraftServer server) {
        AcademyCraftServer.SERVER_NETWORK_MANAGER.registerPacketListener(Server.class);
    }

    public static final class Client {
        public static final AbilitySystemClient.SkillInfo SKILL_INFO =
                AbilityDeveloperScreen.registerSkillInfo(Accelerator.INSTANCE, INSTANCE, List.of(VectorReflection.Client.SKILL_INFO),
                        TextureResources.BLOODFLOW_REVERSE_ICON, 90, 50);
        public static final String KEY_NAME = SkillNames.BLOODFLOW_REVERSE + "_use";
        public static Config CONFIG = new Config();

        public static void reverseBloodflow() {
            NetworkManagerClient.sendPacket(new C2SPacket(new ReverseBloodflowPacket()));
        }

        public static class Config extends KeyBindingConfig {
            public static final class Action implements IConfigAction<Config> {
                public static final IConfigAction<Config> INSTANCE = new Action();

                private Action() {
                }

                @Override
                public @NotNull BloodflowReverse.Client.Config deserialize(@NotNull JsonElement jsonElement, @NotNull Gson gson) {
                    return gson.fromJson(jsonElement, Config.class);
                }

                @Override
                public @NotNull JsonElement serialize(@NotNull BloodflowReverse.Client.Config configInstance, @NotNull Gson gson) {
                    return gson.toJsonTree(configInstance);
                }

                @Override
                public @NotNull BloodflowReverse.Client.Config getDefaultConfig() {
                    return new Config();
                }

                @Override
                public @NotNull Class<Config> getConfigClass() {
                    return Config.class;
                }
            }
        }
    }

    public static final class Server {
        @SuppressWarnings("resource")
        @SubscribePacket
        public static void onAction(ReverseBloodflowPacket packet) {
            ServerPlayer player = packet.packetListenerSupplier.get().getPlayer();
            if (!SkillUse.canActivate(player, INSTANCE, 180.0F)) return;
            Vec3 start = player.getEyePosition();
            Vec3 end = SkillEffects.trace(player, 4.0);
            LivingEntity target = SkillEffects.livingAlongPath(player, start, end, 0.9)
                    .stream()
                    .min(java.util.Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                    .orElse(null);
            if (target == null) return;

            float damage = Math.min(40.0F, Math.max(12.0F, target.getMaxHealth() * 0.45F));
            if (target.hurt(
                    new DamageSource(player.damageSources().damageTypes.getHolderOrThrow(DamageTypes.MAGIC)),
                    damage
            )) {
                SkillUse.complete(player, INSTANCE, 180.0F, 100, 0.003F);
            }
        }
    }

    @PacketTarget(ThreadType.SERVER)
    public static final class ReverseBloodflowPacket extends EmptyPacket<ServerGamePacketListenerImpl> {
    }
}
