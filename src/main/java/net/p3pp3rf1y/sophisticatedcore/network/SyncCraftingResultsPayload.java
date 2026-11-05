package net.p3pp3rf1y.sophisticatedcore.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

import java.util.List;

/**
 * Sends the crafting upgrade's matched result list to the client.
 * <p>
 * 26.3 keeps no client-side recipe manager, so the list cannot be rebuilt on the client and has
 * to come from the server that actually owns the recipe data.
 */
public record SyncCraftingResultsPayload(int upgradeContainerId, List<ItemStack> results) implements CustomPacketPayload {
	public static final Type<SyncCraftingResultsPayload> TYPE = new Type<>(SophisticatedCore.getRL("sync_crafting_results"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncCraftingResultsPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			SyncCraftingResultsPayload::upgradeContainerId,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC,
			SyncCraftingResultsPayload::results,
			SyncCraftingResultsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Environment(EnvType.CLIENT)
	public static void handlePayload(SyncCraftingResultsPayload payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (!(context.player().containerMenu instanceof StorageContainerMenuBase<?> menu)) {
				return;
			}
			UpgradeContainerBase<?, ?> container = menu.getUpgradeContainers().get(payload.upgradeContainerId);
			if (container instanceof CraftingUpgradeContainer craftingContainer) {
				craftingContainer.setMatchedCraftingResults(payload.results);
			}
		});
	}
}
