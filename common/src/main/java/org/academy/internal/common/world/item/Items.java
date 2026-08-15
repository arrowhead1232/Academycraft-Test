package org.academy.internal.common.world.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.academy.internal.common.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Items {
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Item ICON = register("icon", new Item(new Item.Properties()));
    public static final Item DATA_TERMINAL = register("data_terminal", new DataTerminalItem());
    public static final Item COIN = register("coin", new CoinItem());
    public static final Item WIRELESS_NODE = register("wireless_node", new BlockItem(Blocks.WIRELESS_NODE, new Item.Properties()));
    public static final Item WIND_GEN_BASE = register("wind_gen_base", new BlockItem(Blocks.WIND_GEN_BASE, new Item.Properties()));
    public static final Item WIND_GEN_TOP = register("wind_gen_top", new MultiBlockItem(Blocks.WIND_GEN_TOP, new Item.Properties()));
    public static final Item WIND_GEN_PILLAR = register("wind_gen_pillar", new BlockItem(Blocks.WIND_GEN_PILLAR, new Item.Properties()));
    public static final Item ABILITY_DEVELOPER = register("ability_developer", new MultiBlockItem(Blocks.ABILITY_DEVELOPER, new Item.Properties()));
    public static final Item WIND_GEN_FAN_ITEM = register("wind_gen_fan", new Item(new Item.Properties().stacksTo(16)));
    public static final Item IMAGIPHASE_METAL = register("imagiphase_metal", new Item(new Item.Properties()));
    public static final Item IMAGIPHASE_METAL_BLOCK = register("imagiphase_metal_block", new BlockItem(Blocks.IMAGIPHASE_METAL_BLOCK, new Item.Properties()));
    public static final Item EMPTY_UNIT = register("empty_unit", new EmptyUnitItem());
    public static final Item IMAGIPHASE_UNIT = register("imagiphase_unit", new ImagiphaseUnitItem());
    public static final Item WIND_GEN_BASE_SCREEN = register("wind_gen_base_screen", new Item(new Item.Properties()));
    public static final Item IMAGIPHASE_CIRCUIT = register("imagiphase_circuit", new Item(new Item.Properties()));
    public static final Item IMAGIPHASE_POLYMER = register("imagiphase_polymer", new Item(new Item.Properties()));
    public static final Item IMAGIPHASE_PLATE = register("imagiphase_plate", new Item(new Item.Properties()));
    public static final Item IMAGIPHASE_DOWSING_ROD = register("imagiphase_dowsing_rod", new ImagiphaseDowsingRodItem());
    public static final Item IMAGIPHASE_VEGETATION = register("imagiphase_vegetation", new BlockItem(Blocks.IMAGIPHASE_VEGETATION, new Item.Properties()));
    public static final Item IMAGIPHASE_LEAVES = register("imagiphase_leaves", new BlockItem(Blocks.IMAGIPHASE_LEAVES, new Item.Properties()));
    public static final Item IMAGIPHASE_LOG = register("imagiphase_log", new BlockItem(Blocks.IMAGIPHASE_LOG, new Item.Properties()));
    public static final Item IMAGIPHASE_LICHEN = register("imagiphase_lichen", new BlockItem(Blocks.IMAGIPHASE_LICHEN, new Item.Properties()));
    public static final Item OMNI_CRAFTING_TABLE = register("omni_crafting_table", new BlockItem(Blocks.OMNI_CRAFTING_TABLE, new Item.Properties()));
    public static final Item CAT_ENGINE = register("cat_engine", new BlockItem(Blocks.CAT_ENGINE, new Item.Properties()));
    public static final Item IMAGIPHASE_AMETHYST = register("imagiphase_amethyst", new Item(new Item.Properties()));
    public static final Item IMAGIPHASE_AMETHYST_BLOCK = register("imagiphase_amethyst_block", new BlockItem(Blocks.IMAGIPHASE_AMETHYST_BLOCK, new Item.Properties()));

    // Legacy AcademyCraft 1.1.3 component chain. These items deliberately keep
    // their original registry names so old builds, guides and data packs have a
    // stable migration target while the machine progression is rebuilt.
    public static final Item CONSTRAINT_INGOT = register("constraint_ingot", new Item(new Item.Properties()));
    public static final Item CONSTRAINT_PLATE = register("constraint_plate", new Item(new Item.Properties()));
    public static final Item WAFER = register("wafer", new Item(new Item.Properties()));
    public static final Item IMAG_SILICON_PIECE = register("imag_silicon_piece", new Item(new Item.Properties()));
    public static final Item DATA_CHIP = register("data_chip", new Item(new Item.Properties()));
    public static final Item CALC_CHIP = register("calc_chip", new Item(new Item.Properties()));
    public static final Item BRAIN_COMPONENT = register("brain_component", new Item(new Item.Properties()));
    public static final Item INFO_COMPONENT = register("info_component", new Item(new Item.Properties()));
    public static final Item ENERGY_UNIT = register("energy_unit", new Item(new Item.Properties().stacksTo(16)));
    public static final Item ENERGY_CONVERT_COMPONENT = register("energy_convert_component", new Item(new Item.Properties()));

    public static <T extends Item> T register(String name, T item) {
        ITEMS.put(name, item);
        return item;
    }

    private Items() {
    }
}
