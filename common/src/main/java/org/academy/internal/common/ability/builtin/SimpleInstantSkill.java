package org.academy.internal.common.ability.builtin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.academy.AcademyCraftClient;
import org.academy.AcademyCraftConfig;
import org.academy.AcademyCraftServer;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.api.client.config.KeyBindingConfig;
import org.academy.api.client.input.InputSystem;
import org.academy.api.client.network.NetworkManagerClient;
import org.academy.api.client.util.ClientUtil;
import org.academy.api.common.ability.AbilityCategory;
import org.academy.api.common.ability.Skill;
import org.academy.api.common.config.IConfigAction;
import org.academy.api.common.network.PacketTarget;
import org.academy.api.common.network.SubscribePacket;
import org.academy.api.common.network.packet.C2SPacket;
import org.academy.api.common.network.packet.IPacket;
import org.academy.api.common.vanilla.ThreadType;
import org.academy.internal.client.gui.screen.AbilityDeveloperScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Shared, server-authoritative path for the compact 1.1.3 skill ports.
 *
 * <p>The original mod's Context system combined key handling, networking,
 * cooldowns, CP consumption and proficiency. Recreating that entire coremod
 * layer would make every small port depend on unfinished infrastructure, so
 * this class provides the same safety-critical pieces for one-shot skills.</p>
 */
public abstract class SimpleInstantSkill extends Skill {
    private static final Map<String, SimpleInstantSkill> SKILLS = new ConcurrentHashMap<>();
    private static volatile MinecraftServer registeredServer;

    private final Supplier<AbilityCategory> categorySupplier;
    private final ResourceLocation icon;
    private final float treeX;
    private final float treeY;
    private final int defaultKey;
    private final int defaultModifier;
    private final float computingPowerCost;
    private final int cooldownTicks;
    private final float proficiencyGain;

    protected SimpleInstantSkill(
            String name,
            int level,
            int learningEnergy,
            List<Skill> dependencies,
            Supplier<AbilityCategory> categorySupplier,
            ResourceLocation icon,
            float treeX,
            float treeY,
            int defaultKey,
            int defaultModifier,
            float computingPowerCost,
            int cooldownTicks,
            float proficiencyGain
    ) {
        super(name, level, learningEnergy, dependencies);
        this.categorySupplier = categorySupplier;
        this.icon = icon;
        this.treeX = treeX;
        this.treeY = treeY;
        this.defaultKey = defaultKey;
        this.defaultModifier = defaultModifier;
        this.computingPowerCost = computingPowerCost;
        this.cooldownTicks = cooldownTicks;
        this.proficiencyGain = proficiencyGain;
        SKILLS.put(name, this);
    }

    @Override
    public final void initClient() {
        AcademyCraftConfig.registerConfigActions(name, Config.Action.INSTANCE);
        Config config = AcademyCraftClient.CLIENT_CONFIG.getConfig(name);
        if (config == null) {
            config = new Config();
            AcademyCraftClient.CLIENT_CONFIG.setConfig(name, config);
        }

        String keyName = name + "_activate";
        LinkedHashSet<Integer> modifiers = new LinkedHashSet<>();
        if (defaultModifier != 0) {
            modifiers.add(defaultModifier);
        }
        InputSystem.InputPair defaultBinding = new InputSystem.InputPair(
                InputSystem.InputType.KEYBOARD,
                new InputSystem.KeyInfo(
                        new LinkedHashSet<>(Set.of(defaultKey)),
                        GLFW.GLFW_RELEASE,
                        modifiers
                )
        );

        InputSystem.addKeyBinding(keyName, config.getKeyBinding(keyName, defaultBinding), () -> {
            if (ClientUtil.hasScreen() || !AbilitySystemClient.LEARNED_SKILLS.contains(this)) return;
            NetworkManagerClient.sendPacket(new C2SPacket(new ActivatePacket(name)));
        });

        List<AbilitySystemClient.SkillInfo> dependencyInfos = new ArrayList<>();
        for (Skill dependency : dependencies) {
            findSkillInfo(dependency).ifPresent(dependencyInfos::add);
        }
        AbilityDeveloperScreen.registerSkillInfo(
                categorySupplier.get(), this, dependencyInfos, icon, treeX, treeY
        );
    }

    @Override
    public final synchronized void initServer(MinecraftServer server) {
        SkillUse.bindServer(server);
        if (registeredServer != server) {
            AcademyCraftServer.SERVER_NETWORK_MANAGER.registerPacketListener(Server.class);
            registeredServer = server;
        }
    }

    private static java.util.Optional<AbilitySystemClient.SkillInfo> findSkillInfo(Skill skill) {
        return AbilitySystemClient.SKILL_INFOS.values().stream()
                .flatMap(List::stream)
                .filter(info -> info.skill() == skill)
                .findFirst();
    }

    private boolean tryActivate(ServerPlayer player) {
        if (!SkillUse.canActivate(player, this, computingPowerCost)) return false;
        float exp = SkillUse.proficiency(player, this);
        if (!perform(player, exp)) return false;
        SkillUse.complete(player, this, computingPowerCost, cooldownTicks, proficiencyGain);
        return true;
    }

    /** Performs the server-side effect. Return false when no valid action occurred. */
    protected abstract boolean perform(ServerPlayer player, float proficiency);

    public static final class Server {
        @SubscribePacket
        public static void handle(ActivatePacket packet) {
            if (packet.packetListenerSupplier == null) return;
            ServerGamePacketListenerImpl listener = packet.packetListenerSupplier.get();
            if (listener == null) return;
            SimpleInstantSkill skill = SKILLS.get(packet.skillName);
            if (skill != null) {
                skill.tryActivate(listener.getPlayer());
            }
        }
    }

    @PacketTarget(ThreadType.SERVER)
    public static final class ActivatePacket extends IPacket<ServerGamePacketListenerImpl> {
        private String skillName = "";

        public ActivatePacket() {
        }

        public ActivatePacket(String skillName) {
            this.skillName = skillName;
        }

        @Override
        public void read(@NotNull FriendlyByteBuf buf) {
            skillName = buf.readUtf(64);
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
            buf.writeUtf(skillName, 64);
        }
    }

    public static class Config extends KeyBindingConfig {
        public static final class Action implements IConfigAction<Config> {
            public static final Action INSTANCE = new Action();

            private Action() {
            }

            @Override
            public @NotNull Config deserialize(@NotNull JsonElement jsonElement, @NotNull Gson gson) {
                return gson.fromJson(jsonElement, Config.class);
            }

            @Override
            public @NotNull JsonElement serialize(@NotNull Config configInstance, @NotNull Gson gson) {
                return gson.toJsonTree(configInstance);
            }

            @Override
            public @NotNull Config getDefaultConfig() {
                return new Config();
            }

            @Override
            public @NotNull Class<Config> getConfigClass() {
                return Config.class;
            }
        }
    }
}
