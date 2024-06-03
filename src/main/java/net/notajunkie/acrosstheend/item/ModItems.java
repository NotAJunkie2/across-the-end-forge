package net.notajunkie.acrosstheend.item;

import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AcrossTheEnd.MOD_ID);

    public static final RegistryObject<Item> ENDERFLY_ESSENCE = ITEMS.register(
            "enderfly_essence", () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> END_TEAR = ITEMS.register(
            "end_tear", () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> INFUSED_AMETHYST_SHARD = ITEMS.register(
            "infused_amethyst_shard", () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DORMANT_VOID_GRASP_UPGRADE_SMITHING_TEMPLATE = ITEMS.register(
            "dormant_void_grasp_upgrade_smithing_template", () -> new Item(new Item.Properties())
    );


    public static final RegistryObject<SmithingTemplateItem> VOID_GRASP_UPGRADE_SMITHING_TEMPLATE =
        ITEMS.register("void_grasp_upgrade_smithing_template", ModSmithingTemplateItem::createVoidGraspUpgradeTemplate);

    // AMETHYST INFUSED DIAMOND TOOLS
    public static final RegistryObject<Item> AMETHYST_INFUSED_DIAMOND_PICKAXE = ITEMS.register(
            "amethyst_infused_diamond_pickaxe", () -> new PickaxeItem(
                    ModToolTiers.AMETHYST_INFUSED_DIAMOND, 1, -2.8f, new Item.Properties()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_DIAMOND_AXE = ITEMS.register(
            "amethyst_infused_diamond_axe", () -> new AxeItem(
                    ModToolTiers.AMETHYST_INFUSED_DIAMOND, 5.5f, -3f, new Item.Properties()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_DIAMOND_HOE = ITEMS.register(
            "amethyst_infused_diamond_hoe", () -> new HoeItem(
                    ModToolTiers.AMETHYST_INFUSED_DIAMOND, -3, 0f, new Item.Properties()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_DIAMOND_SHOVEL = ITEMS.register(
            "amethyst_infused_diamond_shovel", () -> new ShovelItem(
                    ModToolTiers.AMETHYST_INFUSED_DIAMOND, 1.5f, -3f, new Item.Properties()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_DIAMOND_SWORD = ITEMS.register(
            "amethyst_infused_diamond_sword", () -> new SwordItem(
                    ModToolTiers.AMETHYST_INFUSED_DIAMOND, 3, -2.35f, new Item.Properties()
            )
    );
    // AMETHYST INFUSED NETHERITE
    public static final RegistryObject<Item> AMETHYST_INFUSED_NETHERITE_PICKAXE = ITEMS.register(
            "amethyst_infused_netherite_pickaxe", () -> new PickaxeItem(
                    ModToolTiers.AMETHYST_INFUSED_NETHERITE, 2, -2.8f, new Item.Properties()
                    .fireResistant()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_NETHERITE_AXE = ITEMS.register(
            "amethyst_infused_netherite_axe", () -> new AxeItem(
                    ModToolTiers.AMETHYST_INFUSED_NETHERITE, 6.5f, -2.8f, new Item.Properties()
                    .fireResistant()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_NETHERITE_HOE = ITEMS.register(
            "amethyst_infused_netherite_hoe", () -> new HoeItem(
                    ModToolTiers.AMETHYST_INFUSED_NETHERITE, -3, 0f, new Item.Properties()
                    .fireResistant()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_NETHERITE_SHOVEL = ITEMS.register(
            "amethyst_infused_netherite_shovel", () -> new ShovelItem(
                    ModToolTiers.AMETHYST_INFUSED_NETHERITE, 2.5f, -3f, new Item.Properties()
                    .fireResistant()
            )
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_NETHERITE_SWORD = ITEMS.register(
            "amethyst_infused_netherite_sword", () -> new SwordItem(
                    ModToolTiers.AMETHYST_INFUSED_NETHERITE, 4, -2.25f, new Item.Properties()
                    .fireResistant()
            )
    );
    // Amethyst infused ranged weapons
    public static final RegistryObject<Item> AMETHYST_INFUSED_BOW = ITEMS.register(
            "amethyst_infused_bow", () -> new BowItem(new Item.Properties().durability(480))
    );
    public static final RegistryObject<Item> AMETHYST_INFUSED_CROSSBOW = ITEMS.register(
            "amethyst_infused_crossbow", () -> new CrossbowItem(new Item.Properties().durability(408))
    );

    // Spawn eggs
    public static final RegistryObject<SpawnEggItem> ENDERFLY_SPAWN_EGG = ITEMS.register(
            "enderfly_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ENDERFLY, 0x161616, 0xAF91E7, new Item.Properties())
    );
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
