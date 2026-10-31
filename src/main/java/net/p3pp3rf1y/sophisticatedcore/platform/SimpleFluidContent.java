package net.p3pp3rf1y.sophisticatedcore.platform;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Predicate;

/**
 * Data component payload holding a {@link FluidStack}, so tanks and fluid filters can live on an item.
 */
public class SimpleFluidContent implements DataComponentHolder {
	public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
	public static final Codec<SimpleFluidContent> CODEC = FluidStack.OPTIONAL_CODEC
			.xmap(SimpleFluidContent::new, content -> content.fluidStack);
	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC
			.map(SimpleFluidContent::new, content -> content.fluidStack);

	private final FluidStack fluidStack;

	private SimpleFluidContent(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
	}

	public static SimpleFluidContent copyOf(FluidStack fluidStack) {
		return fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.copy());
	}

	public FluidStack copy() {
		return fluidStack.copy();
	}

	public FluidStack getFluidStack() {
		return fluidStack;
	}

	public boolean isEmpty() {
		return fluidStack.isEmpty();
	}

	public Fluid getFluid() {
		return fluidStack.getFluid();
	}

	public Holder<Fluid> getFluidHolder() {
		return fluidStack.getFluidHolder();
	}

	public FluidVariant getVariant() {
		return fluidStack.getVariant();
	}

	public long getAmount() {
		return fluidStack.getAmount();
	}

	public boolean is(TagKey<Fluid> tag) {
		return fluidStack.is(tag);
	}

	public boolean is(Fluid fluid) {
		return fluidStack.is(fluid);
	}

	public boolean is(Predicate<Holder<Fluid>> predicate) {
		return fluidStack.is(predicate);
	}

	public boolean is(Holder<Fluid> holder) {
		return fluidStack.is(holder);
	}

	public boolean is(HolderSet<Fluid> holders) {
		return fluidStack.is(holders);
	}

	public boolean matches(FluidStack other) {
		return FluidStack.matches(fluidStack, other);
	}

	public boolean isSameFluid(FluidStack other) {
		return FluidStack.isSameFluid(fluidStack, other);
	}

	public boolean isSameFluidSameComponents(FluidStack other) {
		return FluidStack.isSameFluidSameComponents(fluidStack, other);
	}

	public boolean isSameFluidSameComponents(SimpleFluidContent content) {
		return isSameFluidSameComponents(content.fluidStack);
	}

	@Override
	public DataComponentMap getComponents() {
		return fluidStack.getComponents();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof SimpleFluidContent other && FluidStack.matches(fluidStack, other.fluidStack);
	}

	@Override
	public int hashCode() {
		return (int) (fluidStack.getAmount() * 31 + FluidStack.hashFluidAndComponents(fluidStack));
	}
}
