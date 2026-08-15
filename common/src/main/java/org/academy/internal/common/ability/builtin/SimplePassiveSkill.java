package org.academy.internal.common.ability.builtin;

import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.api.common.ability.AbilityCategory;
import org.academy.api.common.ability.Skill;
import org.academy.internal.client.gui.screen.AbilityDeveloperScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Registers a learned passive in the developer tree without creating a key binding. */
public abstract class SimplePassiveSkill extends Skill {
    private final Supplier<AbilityCategory> categorySupplier;
    private final ResourceLocation icon;
    private final float treeX;
    private final float treeY;

    protected SimplePassiveSkill(
            String name,
            int level,
            int learningEnergy,
            List<Skill> dependencies,
            Supplier<AbilityCategory> categorySupplier,
            ResourceLocation icon,
            float treeX,
            float treeY
    ) {
        super(name, level, learningEnergy, dependencies);
        this.categorySupplier = categorySupplier;
        this.icon = icon;
        this.treeX = treeX;
        this.treeY = treeY;
    }

    @Override
    public final void initClient() {
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
}
