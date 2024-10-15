package net.notajunkie.acrosstheend.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.entity.ModEntities;

import java.util.List;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> SPAWN_ENDERFLY = registerKey("spawn_enderfly");
    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(SPAWN_ENDERFLY, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                // Spawn only in the end highlands and midlands
                HolderSet.direct(biomes.getOrThrow(Biomes.END_HIGHLANDS), biomes.getOrThrow(Biomes.END_MIDLANDS)),
                List.of(new MobSpawnSettings.SpawnerData(ModEntities.ENDERFLY.get(), 50, 5, 15))));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(AcrossTheEnd.MOD_ID, name));
    }
 }
