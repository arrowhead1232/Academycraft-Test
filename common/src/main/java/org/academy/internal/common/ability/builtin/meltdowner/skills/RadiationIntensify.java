package org.academy.internal.common.ability.builtin.meltdowner.skills;

import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimplePassiveSkill;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;

import java.util.List;

/** Passive irradiation mark that amplifies subsequent Meltdowner damage. */
public final class RadiationIntensify extends SimplePassiveSkill {
    public static final RadiationIntensify INSTANCE = new RadiationIntensify();

    private RadiationIntensify() {
        super(
                SkillNames.RADIATION_INTENSIFY, 1, 5000, List.of(ElectronBomb.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.RADIATION_INTENSIFY_ICON,
                35, 75
        );
    }
}
