package net.p3pp3rf1y.sophisticatedcore.network;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.util.StreamCodecHelper;

import org.jetbrains.annotations.Nullable;

public record SyncContainerClientDataPayload(@Nullable CompoundTag data) implements CustomPacketPayload {
	public static final Type<SyncContainerClientDataPayload> TYPE = new Type<>(SophisticatedCore.getRL("sync_container_client_data"));
	public static final StreamCodec<ByteBuf, SyncContainerClientDataPayload> STREAM_CODEC = StreamCodec.composite(
			StreamCodecHelper.ofNullable(ByteBufCodecs.COMPOUND_TAG),
			SyncContainerClientDataPayload::data,
			SyncContainerClientDataPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(SyncContainerClientDataPayload payload, ServerPlayNetworking.Context context) {
		if (payload.data == null) {
			return;
		}

		if (context.player().containerMenu instanceof ISyncedContainer container) {
			SophisticatedCore.LOGGER.debug("Received container client data {} for {}", payload.data.keySet(), container.getClass().getSimpleName());
			container.handlePacket(payload.data);
		} else {
			// 打出来是为了区分“包没到”和“到了但当前菜单不是 ISyncedContainer（比如设置界面顶掉了内容界面）”
			SophisticatedCore.LOGGER.debug("Received container client data {} but open menu {} is not ISyncedContainer",
					payload.data.keySet(), context.player().containerMenu.getClass().getSimpleName());
		}
	}
}
