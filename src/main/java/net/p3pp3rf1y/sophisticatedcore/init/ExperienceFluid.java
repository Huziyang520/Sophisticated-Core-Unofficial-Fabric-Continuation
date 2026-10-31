package net.p3pp3rf1y.sophisticatedcore.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Experience fluid. 26.3 removed {@code FluidType}, so the properties that used to live there
 * (light level, density, viscosity) are now supplied by the fluid itself and by the Fabric
 * fluid attribute handler.
 */
public abstract class ExperienceFluid extends FlowingFluid {
	public static final int LIGHT_LEVEL = 10;
	public static final int DENSITY = 800;
	public static final int VISCOSITY = 1500;

	@Override
	public Fluid getFlowing() {
		return ModFluids.XP_FLOWING.get();
	}

	@Override
	public Fluid getSource() {
		return ModFluids.XP_STILL.get();
	}

	@Override
	public Item getBucket() {
		return ModFluids.XP_BUCKET.get();
	}

	@Override
	protected boolean canConvertToSource(ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		// Experience fluid does not drop a block source, matching the original behaviour.
	}

	@Override
	protected int getSlopeFindDistance(LevelReader level) {
		return 2;
	}

	@Override
	protected int getDropOff(LevelReader level) {
		return 2;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 20;
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
		return direction == Direction.DOWN && !isSame(fluid);
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == ModFluids.XP_STILL.get() || fluid == ModFluids.XP_FLOWING.get();
	}

	@Override
	protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
		return Vec3.ZERO;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return ModFluids.XP_STILL.get().defaultFluidState().createLegacyBlock();
	}

	public static class Source extends ExperienceFluid {
		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends ExperienceFluid {
		@Override
		protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}
}
