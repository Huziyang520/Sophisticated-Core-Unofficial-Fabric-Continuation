package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tool capability facade.
 * <p>
 * 26.3 dropped the {@code ItemAbility} model: axes, pickaxes, shovels and hoes are now plain
 * items whose behaviour comes from the {@code minecraft:tool} component and item tags. Callers
 * that only need "is this a digging tool / an axe / a sword" get the same answers here.
 */
public final class ToolAbilities {
	private ToolAbilities() {
	}

	/**
	 * True for items that can dig or modify blocks: axes, hoes, pickaxes, shovels and shears.
	 */
	public static boolean isDiggingTool(ItemStack stack) {
		return isAxe(stack) || isHoe(stack) || isPickaxe(stack) || isShovel(stack) || isShears(stack);
	}

	public static boolean isAxe(ItemStack stack) {
		return stack.is(ItemTags.AXES);
	}

	public static boolean isHoe(ItemStack stack) {
		return stack.is(ItemTags.HOES);
	}

	public static boolean isPickaxe(ItemStack stack) {
		return stack.is(ItemTags.PICKAXES);
	}

	public static boolean isShovel(ItemStack stack) {
		return stack.is(ItemTags.SHOVELS);
	}

	public static boolean isShears(ItemStack stack) {
		return stack.getItem() instanceof ShearsItem;
	}

	public static boolean isSword(ItemStack stack) {
		return stack.is(ItemTags.SWORDS);
	}

	/**
	 * True when the stack carries tool rules at all, i.e. it is meant to be used on blocks.
	 */
	public static boolean hasToolComponent(ItemStack stack) {
		return stack.get(DataComponents.TOOL) != null;
	}

	/**
	 * True when this tool is the correct one for harvesting the given state.
	 */
	public static boolean isCorrectForDrops(ItemStack stack, BlockState state) {
		return stack.getItem().isCorrectToolForDrops(stack, state);
	}

	/**
	 * Mining speed the tool would get on this state, or 0 when it has no tool rules.
	 */
	public static float getMiningSpeed(ItemStack stack, BlockState state) {
		Tool tool = stack.get(DataComponents.TOOL);
		return tool == null ? 0F : tool.getMiningSpeed(state);
	}
}
