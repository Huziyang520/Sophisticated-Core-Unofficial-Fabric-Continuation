package net.p3pp3rf1y.sophisticatedcore.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A {@link FluidStack} that is meant to be stored in a data component, so it carries codecs and is
 * immutable in practice.
 * <p>
 * Replacement for Porting Lib's {@code SimpleFluidContent}.
 */
public class SimpleFluidContent extends FluidStack {
	public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidVariant.blank(), 0);

	public static final Codec<SimpleFluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FluidVariant.CODEC.fieldOf("fluid").forGetter(SimpleFluidContent::getVariant),
			Codec.LONG.optionalFieldOf("amount", 0L).forGetter(SimpleFluidContent::getAmount)
	).apply(instance, SimpleFluidContent::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = StreamCodec.composite(
			FluidVariant.PACKET_CODEC, SimpleFluidContent::getVariant,
			ByteBufCodecs.VAR_LONG, SimpleFluidContent::getAmount,
			SimpleFluidContent::new);

	public SimpleFluidContent(FluidVariant fluidVariant, long amount) {
		super(fluidVariant, amount);
	}

	public SimpleFluidContent(FluidStack fluidStack) {
		super(fluidStack.getVariant(), fluidStack.getAmount());
	}

	/** @return the given stack captured as component content; {@link #EMPTY} when there is no fluid. */
	public static SimpleFluidContent copyOf(FluidStack fluidStack) {
		return fluidStack == null || fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.getVariant(), fluidStack.getAmount());
	}

	@Override
	public SimpleFluidContent copy() {
		return new SimpleFluidContent(getVariant(), getAmount());
	}
}
