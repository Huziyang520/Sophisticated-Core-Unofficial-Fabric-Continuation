package net.p3pp3rf1y.sophisticatedcore.upgrades;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.p3pp3rf1y.sophisticatedcore.platform.FluidStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public interface IRenderedTankUpgrade {
	void setTankRenderInfoUpdateCallback(Consumer<TankRenderInfo> updateTankRenderInfoCallback);

	void forceUpdateTankRenderInfo();

	class TankRenderInfo {
		private static final String FLUID_TAG = "fluid";
		private static final String AMOUNT_TAG = "amount";
		private static final String FILL_RATIO_TAG = "fillRatio";

		public TankRenderInfo() {
			this(null, 0);
		}

		public TankRenderInfo(@Nullable FluidStack fluidStack, float fillRatio) {
			this.fluidStack = fluidStack;
			this.fillRatio = fillRatio;
		}

		@Nullable
		private FluidStack fluidStack;
		private float fillRatio;

		public CompoundTag serialize() {
			CompoundTag ret = new CompoundTag();
			if (fluidStack != null && !fluidStack.isEmpty()) {
				ret.putString(FLUID_TAG, BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString());
				ret.putLong(AMOUNT_TAG, fluidStack.getAmount());
				ret.putFloat(FILL_RATIO_TAG, fillRatio);
			}
			return ret;
		}

		public static TankRenderInfo deserialize(CompoundTag tag) {
			String fluidId = tag.getStringOr(FLUID_TAG, "");
			long amount = tag.getLongOr(AMOUNT_TAG, 0);
			if (fluidId.isEmpty() && tag.contains(FLUID_TAG)) {
				CompoundTag legacy = tag.getCompoundOrEmpty(FLUID_TAG);
				fluidId = legacy.getStringOr("id", "");
				amount = legacy.getLongOr("amount", 0);
			}
			if (!fluidId.isEmpty() && amount > 0) {
				Identifier id = Identifier.tryParse(fluidId);
				Fluid fluid = id == null ? null : BuiltInRegistries.FLUID.getOptional(id).orElse(null);
				if (fluid != null && fluid != Fluids.EMPTY) {
					return new TankRenderInfo(new FluidStack(fluid, amount), tag.getFloatOr(FILL_RATIO_TAG, 0F));
				}
			}
			return new TankRenderInfo();
		}

		public void setFluid(FluidStack fluidStack) {
			this.fluidStack = fluidStack.copy();
		}

		public Optional<FluidStack> getFluid() {
			return Optional.ofNullable(fluidStack);
		}

		public void setFillRatio(float fillRatio) {
			this.fillRatio = fillRatio;
		}

		public float getFillRatio() {
			return fillRatio;
		}
	}
}
