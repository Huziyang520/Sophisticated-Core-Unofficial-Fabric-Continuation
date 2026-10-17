package net.p3pp3rf1y.sophisticatedcore.init;

import net.p3pp3rf1y.sophisticatedcore.platform.DeferredRegister;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalFluidTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

import java.util.function.Supplier;

public class ModFluids {
	private ModFluids() {
	}

	public static final TagKey<Fluid> EXPERIENCE_TAG = ConventionalFluidTags.EXPERIENCE;
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, SophisticatedCore.MOD_ID);

	public static final Supplier<FlowingFluid> XP_STILL = FLUIDS.register("xp_still", ExperienceFluid.Source::new);

	public static final Supplier<FlowingFluid> XP_FLOWING = FLUIDS.register("xp_flowing", ExperienceFluid.Flowing::new);

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SophisticatedCore.MOD_ID);
	public static final Supplier<Item> XP_BUCKET = ITEMS.register("xp_bucket", () -> new BucketItem(XP_STILL.get(), net.p3pp3rf1y.sophisticatedcore.platform.RegistryIdContext.applyToItem(new Item.Properties()).stacksTo(1)));

	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SophisticatedCore.MOD_ID);
	public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
			FabricCreativeModeTab.builder().icon(() -> new ItemStack(XP_BUCKET.get()))
					.title(Component.translatable("itemGroup.sophisticatedcore"))
					.displayItems((featureFlags, output) -> output.accept(new ItemStack(XP_BUCKET.get())))
					.build());

	public static void registerHandlers() {
		FLUIDS.register();
		ITEMS.register();
		CREATIVE_MODE_TABS.register();
	}
}
