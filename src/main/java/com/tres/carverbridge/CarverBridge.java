package com.tres.carverbridge;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mod(CarverBridge.MODID)
public class CarverBridge {
    public static final String MODID = "tres_carver_bridge";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Los mismos carvers que WF's Cave Overhaul agrega por biome modifier.
    private static final String[] WF_CARVERS = {
            "caveoverhaul:caves_noise_distribution",
            "caveoverhaul:canyons",
            "caveoverhaul:canyons_low_y"
    };

    private static boolean loggedOnce = false;

    public CarverBridge() {
        LOGGER.info("[tres_carver_bridge] cargado");
    }

    /** Llamado por el mixin: devuelve los carvers del bioma + los de WF si faltan. */
    public static Iterable<Holder<ConfiguredWorldCarver<?>>> merge(Iterable<Holder<ConfiguredWorldCarver<?>>> original,
                                                                    GenerationStep.Carving step) {
        try {
            if (step != GenerationStep.Carving.AIR) return original;
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return original;
            Registry<ConfiguredWorldCarver<?>> reg = server.registryAccess().registryOrThrow(Registries.CONFIGURED_CARVER);

            List<Holder<ConfiguredWorldCarver<?>>> out = new ArrayList<>();
            Set<ResourceLocation> present = new HashSet<>();
            for (Holder<ConfiguredWorldCarver<?>> h : original) {
                out.add(h);
                h.unwrapKey().ifPresent(k -> present.add(k.location()));
            }
            int added = 0;
            for (String id : WF_CARVERS) {
                ResourceLocation rl = new ResourceLocation(id);
                if (present.contains(rl)) continue;
                Optional<Holder.Reference<ConfiguredWorldCarver<?>>> opt =
                        reg.getHolder(ResourceKey.create(Registries.CONFIGURED_CARVER, rl));
                if (opt.isPresent()) {
                    out.add(opt.get());
                    added++;
                }
            }
            if (!loggedOnce) {
                loggedOnce = true;
                LOGGER.info("[tres_carver_bridge] Moderner Beta carvers del bioma: {} | agregados de WF: {}", present, added);
            }
            return out;
        } catch (Throwable t) {
            if (!loggedOnce) {
                loggedOnce = true;
                LOGGER.error("[tres_carver_bridge] error", t);
            }
            return original;
        }
    }
}
