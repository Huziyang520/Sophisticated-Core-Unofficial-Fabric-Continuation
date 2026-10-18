package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.Config;

import java.util.function.Consumer;

public class BlockItemBase extends BlockItem {
	public BlockItemBase(Block block, Properties properties) {
		// 26.3 删掉了 BlockItem.getDescriptionId() 的 block.* 前缀，改由 Properties.useBlockDescriptionPrefix() 承担；
		// 不写会导致方块物品名字回落成 item.<ns>.<id> 原始键（lang 里只有 block.* 键）
		super(block, net.p3pp3rf1y.sophisticatedcore.platform.RegistryIdContext.applyToItem(properties.useBlockDescriptionPrefix()));
	}

	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this) && getBlock() instanceof BlockBase blockBase) {
			blockBase.addCreativeTabItems(itemConsumer);
		}
	}
}
