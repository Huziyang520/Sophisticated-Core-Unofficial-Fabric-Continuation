package net.p3pp3rf1y.sophisticatedcore.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.p3pp3rf1y.sophisticatedcore.init.ModFluids;

import java.util.concurrent.CompletableFuture;

public class SCFluidTagsProvider extends FabricTagsProvider.FluidTagsProvider {
	public SCFluidTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(output, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		builder(ModFluids.EXPERIENCE_TAG).add(ModFluids.XP_STILL.get());
	}
}