package org.academy.internal.common.ability.builtin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.bus.api.SubscribeEvent;
import org.academy.AcademyCraft;
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
import org.academy.api.server.vanilla.ServerTickEvent;
import org.academy.internal.client.gui.screen.AbilityDeveloperScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Shared press/hold/release implementation for sustained server skills. */
public abstract class SimpleSustainedSkill extends Skill {
    private static final Map<String, SimpleSustainedSkill> SKILLS = new ConcurrentHashMap<>();
    private static final Map<UUID, ActiveUse> ACTIVE_USES = new ConcurrentHashMap<>();
    private static final AtomicBoolean TICK_LISTENER_REGISTERED = new AtomicBoolean();
    private static volatile MinecraftServer registeredServer;

    private final Supplier<AbilityCategory> categorySupplier;
    private final ResourceLocation icon;
    private final float treeX;
    private final float treeY;
    private final int defaultKey;
    private final int defaultModifier;
    private final float computingPowerPerTick;
    private final int cooldownTicks;
    private final float proficiencyPerTick;

    protected SimpleSustainedSkill(
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
            float computingPowerPerTick,
            int cooldownTicks,
            float proficiencyPerTick
    ) {
        super(name, level, learningEnergy, dependencies);
        this.categorySupplier = categorySupplier;
        this.icon = icon;
        this.treeX = treeX;
        this.treeY = treeY;
        this.defaultKey = defaultKey;
        this.defaultModifier = defaultModifier;
        this.computingPowerPerTick = computingPowerPerTick;
        this.cooldownTicks = cooldownTicks;
        this.proficiencyPerTick = proficiencyPerTick;
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

        LinkedHashSet<Integer> modifiers = new LinkedHashSet<>();
        if (defaultModifier != 0) modifiers.add(defaultModifier);
        LinkedHashSet<Integer> inputs = new LinkedHashSet<>(Set.of(defaultKey));
        String startName = name + "_start";
        String stopName = name + "_stop";

        InputSystem.addKeyBinding(startName, config.getKeyBinding(
                startName,
                new InputSystem.InputPair(
                        InputSystem.InputType.KEYBOARD,
                        new InputSystem.KeyInfo(inputs, GLFW.GLFW_PRESS, modifiers)
                )
        ), () -> sendControl(true));
        InputSystem.addKeyBinding(stopName, config.getKeyBinding(
                stopName,
                new InputSystem.InputPair(
                        InputSystem.InputType.KEYBOARD,
                        new InputSystem.KeyInfo(
                                new LinkedHashSet<>(inputs), GLFW.GLFW_RELEASE,
                                new LinkedHashSet<>(modifiers)
                        )
                )
        ), () -> sendControl(false));

        List<AbilitySystemClient.SkillInfo> dependencyInfos = new ArrayList<>();
        for (Skill dependency : dependencies) {
            AbilitySystemClient.SKILL_INFOS.values().stream()
                    .flatMap(List::stream)
                    .filter(info -> info.skill() == dependency)
                    .findFirst()
                    .ifPresent(dependencyInfos::add);
        }
        AbilityDeveloperScreen.registerSkillInfo(
                categorySupplier.get(), this, dependencyInfos, icon, treeX, treeY
        );
    }

    private void sendControl(boolean start) {
        if (ClientUtil.hasScreen() || !AbilitySystemClient.LEARNED_SKILLS.contains(this)) return;
        NetworkManagerClient.sendPacket(new C2SPacket(new ControlPacket(name, start)));
    }

    @Override
    public final synchronized void initServer(MinecraftServer server) {
        SkillUse.bindServer(server);
        if (registeredServer != server) {
            ACTIVE_USES.clear();
            AcademyCraftServer.SERVER_NETWORK_MANAGER.registerPacketListener(Server.class);
            registeredServer = server;
        }
        if (TICK_LISTENER_REGISTERED.compareAndSet(false, true)) {
            AcademyCraft.EVENT_BUS.register(Server.class);
        }
    }

    /** Return non-null state when the sustained action can begin. */
    protected abstract Object begin(ServerPlayer player, float proficiency);

    /** Return false to terminate the action. */
    protected abstract boolean tick(ServerPlayer player, float proficiency, Object state, int ticks);

    protected void end(ServerPlayer player, Object state, int ticks) {
    }

    private static void start(ServerPlayer player, SimpleSustainedSkill skill) {
        stop(player);
        if (!SkillUse.canActivate(player, skill, skill.computingPowerPerTick)) return;
        float proficiency = SkillUse.proficiency(player, skill);
        Object state = skill.begin(player, proficiency);
        if (state != null) {
            ACTIVE_USES.put(player.getUUID(), new ActiveUse(player, skill, state));
        }
    }

    private static void stop(ServerPlayer player) {
        ActiveUse active = ACTIVE_USES.remove(player.getUUID());
        if (active == null) return;
        active.skill.end(active.player, active.state, active.ticks);
        if (active.ticks > 0) {
            SkillUse.setCooldown(active.player, active.skill, active.skill.cooldownTicks);
        }
    }

    private static void tickActive(ActiveUse active) {
        ServerPlayer player = active.player;
        SimpleSustainedSkill skill = active.skill;
        if (player.isRemoved() || !player.isAlive() || !SkillUse.owns(player, skill)) {
            stop(player);
            return;
        }
        if (!SkillUse.consumeComputingPower(player, skill.computingPowerPerTick)) {
            stop(player);
            return;
        }
        float proficiency = SkillUse.proficiency(player, skill);
        if (!skill.tick(player, proficiency, active.state, active.ticks)) {
            stop(player);
            return;
        }
        active.ticks++;
        if (active.ticks % 20 == 0) {
            SkillUse.grantProficiency(player, skill, skill.proficiencyPerTick * 20.0F);
        }
    }

    private static final class ActiveUse {
        private final ServerPlayer player;
        private final SimpleSustainedSkill skill;
        private final Object state;
        private int ticks;

        private ActiveUse(ServerPlayer player, SimpleSustainedSkill skill, Object state) {
            this.player = player;
            this.skill = skill;
            this.state = state;
        }
    }

    public static final class Server {
        @SubscribePacket
        public static void handle(ControlPacket packet) {
            if (packet.packetListenerSupplier == null) return;
            ServerGamePacketListenerImpl listener = packet.packetListenerSupplier.get();
            if (listener == null) return;
            ServerPlayer player = listener.getPlayer();
            SimpleSustainedSkill skill = SKILLS.get(packet.skillName);
            if (packet.start && skill != null) start(player, skill);
            else stop(player);
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent event) {
            for (ActiveUse active : List.copyOf(ACTIVE_USES.values())) {
                tickActive(active);
            }
        }
    }

    @PacketTarget(ThreadType.SERVER)
    public static final class ControlPacket extends IPacket<ServerGamePacketListenerImpl> {
        private String skillName = "";
        private boolean start;

        public ControlPacket() {
        }

        public ControlPacket(String skillName, boolean start) {
            this.skillName = skillName;
            this.start = start;
        }

        @Override
        public void read(@NotNull FriendlyByteBuf buf) {
            skillName = buf.readUtf(64);
            start = buf.readBoolean();
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
            buf.writeUtf(skillName, 64);
            buf.writeBoolean(start);
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
