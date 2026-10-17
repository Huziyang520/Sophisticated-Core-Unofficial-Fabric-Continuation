package net.p3pp3rf1y.sophisticatedcore.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * The experience fluid.
 * <p>
 * 26.3 removed the {@code FluidType} property holder and Porting Lib's {@code BaseFlowingFluid}, so the fluid is a plain
 * {@link FlowingFluid} modelled on vanilla water. The old type properties (light level 10, density 800, viscosity 1500)
 * have no equivalent: fluid behaviour is data driven now and there is no per-fluid density or viscosity knob.
 */
public abstract class ExperienceFluid extends FlowingFluid {
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
		Block.dropResources(state, level, pos);
	}

	@Override
	public int getSlopeFindDistance(LevelReader level) {
		return 4;
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 1;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == ModFluids.XP_STILL.get() || fluid == ModFluids.XP_FLOWING.get();
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
		return direction == Direction.DOWN && !fluid.is(ModFluids.EXPERIENCE_TAG);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
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
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
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
