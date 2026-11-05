package net.p3pp3rf1y.sophisticatedcore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

/**
 * Asks the server for the crafting upgrade's matched results when the client menu opens.
 * <p>
 * The server computes the list while opening the menu, before the client screen exists, so a
 * pushed update can arrive too early and be dropped. Requesting it from the client guarantees the
 * receiving menu is already constructed.
 */
public record RequestCraftingResultsPayload(int upgradeContainerId) implements CustomPacketPayload {
	public static final Type<RequestCraftingResultsPayload> TYPE = new Type<>(SophisticatedCore.getRL("request_crafting_results"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestCraftingResultsPayload> STREAM_CODEC = StreamCodec.of(
			(buffer, payload) -> buffer.writeVarInt(payload.upgradeContainerId()),
			buffer -> new RequestCraftingResultsPayload(buffer.readVarInt()));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(RequestCraftingResultsPayload payload, ServerPlayNetworking.Context context) {
		if (!(context.player().containerMenu instanceof StorageContainerMenuBase<?> menu)) {
			return;
		}
		UpgradeContainerBase<?, ?> container = menu.getUpgradeContainers().get(payload.upgradeContainerId);
		if (container instanceof CraftingUpgradeContainer craftingContainer) {
			craftingContainer.sendCraftingResults();
		}
	}
}
