package org.academy.internal.common.ability.builtin.electromaster;

import org.academy.api.common.ability.AbilityCategory;
import org.academy.internal.common.ability.builtin.AbilityCategoryNames;
import org.academy.internal.common.ability.builtin.electromaster.skills.*;

public final class Electromaster extends AbilityCategory {
    public static final AbilityCategory INSTANCE = new Electromaster();

    private Electromaster() {
        super(AbilityCategoryNames.ELECTROMASTER);
        this.skillList.add(ArcGenerate.INSTANCE);
        this.skillList.add(CurrentCharging.INSTANCE);
        this.skillList.add(MagneticMovement.INSTANCE);
        this.skillList.add(MagnetManipulation.INSTANCE);
        this.skillList.add(MineDetect.INSTANCE);
        this.skillList.add(BodyIntensify.INSTANCE);
        this.skillList.add(ThunderBolt.INSTANCE);
        this.skillList.add(Railgun.INSTANCE);
        this.skillList.add(ThunderClap.INSTANCE);
    }
}
