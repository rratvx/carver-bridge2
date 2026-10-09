package com.tres.carverbridge.mixin;

import com.tres.carverbridge.CarverBridge;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "mod.bluestaggo.modernerbeta.level.chunk.ModernBetaChunkGenerator", remap = false)
public class ModernBetaChunkGeneratorMixin {

    // applyCarvers (SRG m_213679_) llama a BiomeGenerationSettings.getCarvers (SRG m_204187_).
    @Redirect(
            method = "m_213679_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/BiomeGenerationSettings;m_204187_(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)Ljava/lang/Iterable;",
                    remap = false),
            remap = false,
            require = 0)
    private Iterable<Holder<ConfiguredWorldCarver<?>>> carverbridge$addWf(BiomeGenerationSettings settings,
                                                                          GenerationStep.Carving step) {
        return CarverBridge.merge(settings.getCarvers(step), step);
    }
}
