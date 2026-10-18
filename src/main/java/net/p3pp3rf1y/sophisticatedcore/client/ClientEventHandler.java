package net.p3pp3rf1y.sophisticatedcore.client;

import org.joml.Matrix3x2fStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.client.init.ModParticles;
import net.p3pp3rf1y.sophisticatedcore.client.render.ModFluidRendering;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientRecipesUpdated;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StorageSoundHandler;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class ClientEventHandler implements ClientModInitializer {
	public ClientEventHandler() {
	}

    @Override
    public void onInitializeClient() {
        ModParticles.registerFactories();
        ModFluidRendering.register();

        ServerLevelEvents.UNLOAD.register(StorageSoundHandler::onWorldUnload);
		ClientTickEvents.END_LEVEL_TICK.register(StorageSoundHandler::tick);

        ClientPlayConnectionEvents.JOIN.register(ClientEventHandler::onPlayerJoinServer);
		ClientRecipesUpdated.EVENT.register(RecipeHelper::onRecipesUpdated);

        ScreenEvents.BEFORE_INIT.register((client, screen, windowWidth, windowHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?>) || screen instanceof CreativeModeInventoryScreen || client.player == null) {
                return;
            }

			ScreenEvents.afterExtract(screen).register(ClientEventHandler::onDrawScreen);
		});
	}

	private static void onDrawScreen(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
		Minecraft mc = Minecraft.getInstance();
		if (!(screen instanceof AbstractContainerScreen<?> containerGui) || screen instanceof CreativeModeInventoryScreen || mc.player == null) {
			return;
		}
		AbstractContainerMenu menu = containerGui.getMenu();
		ItemStack held = menu.getCarried();
		if (!held.isEmpty()) {
			Slot under = containerGui.sophisticatedCore_getSlotUnderMouse();

			List<Slot> slots = menu instanceof StorageContainerMenuBase<?> storageMenu ? storageMenu.realInventorySlots : menu.slots;

			for (Slot s : slots) {
				ItemStack stack = s.getItem();
				if (!s.mayPickup(mc.player) || stack.isEmpty()) {
					continue;
				}
				Optional<StashResultAndTooltip> stashResultAndTooltip = getStashResultAndTooltip(stack, held);
				if (stashResultAndTooltip.isEmpty()) {
					continue;
				}

				if (s == under) {
					renderSpecialTooltip(mc, guiGraphics, mouseX, mouseY, stashResultAndTooltip.get());
				} else {
					renderStashSign(mc, containerGui, guiGraphics, s, stack, stashResultAndTooltip.get().stashResult());
				}
			}
		}
	}

	private static void renderStashSign(Minecraft mc, AbstractContainerScreen<?> containerGui, GuiGraphicsExtractor guiGraphics, Slot s, ItemStack stack, IStashStorageItem.StashResult stashResult) {
		int x = containerGui.sophisticatedCore_getGuiLeft() + s.x;
		int y = containerGui.sophisticatedCore_getGuiTop() + s.y;

		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		// Because of trinkets we need to increase this from the original 300
		// Trinkets uses 310, so 330 was chosen as 320 was not enough as it cut the plus sign in half
		poseStack.translate(0, 0);

		int color = stashResult == IStashStorageItem.StashResult.MATCH_AND_SPACE ? 0x55FF55 : 0xFFFF00;
		if (stack.getItem() instanceof IStashStorageItem) {
			guiGraphics.text(mc.font, "+", x + 10, y + 8, color);
		} else {
			guiGraphics.text(mc.font, "-", x + 1, y, color);
		}
		poseStack.popMatrix();
	}

    private static void renderSpecialTooltip(Minecraft mc, GuiGraphicsExtractor guiGraphics, int x, int y, StashResultAndTooltip stashResultAndTooltip) {
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(0, 0);
		guiGraphics.setTooltipForNextFrame(mc.font, Collections.singletonList(Component.translatable(TranslationHelper.INSTANCE.translItemTooltip("storage") + ".right_click_to_add_to_storage")), stashResultAndTooltip.tooltip(), x, y);
		poseStack.popMatrix();
	}

	private static Optional<StashResultAndTooltip> getStashResultAndTooltip(ItemStack inInventory, ItemStack held) {
		if (inInventory.getCount() == 1 && inInventory.getItem() instanceof IStashStorageItem stashStorageItem) {
			return getStashResultAndTooltip(inInventory, held, stashStorageItem);
		}

		if (held.getItem() instanceof IStashStorageItem stashStorageItem) {
			return getStashResultAndTooltip(held, inInventory, stashStorageItem);
		}
		return Optional.empty();
	}

	@NotNull
	private static Optional<StashResultAndTooltip> getStashResultAndTooltip(ItemStack potentialStashStorage, ItemStack potentiallyStashable, IStashStorageItem stashStorageItem) {
		IStashStorageItem.StashResult stashResult = stashStorageItem.getItemStashable(Minecraft.getInstance().level.registryAccess(), potentialStashStorage, potentiallyStashable);
		if (stashResult == IStashStorageItem.StashResult.NO_SPACE) {
			return Optional.empty();
		}
		return Optional.of(new StashResultAndTooltip(stashResult, stashStorageItem.getInventoryTooltip(potentialStashStorage)));
	}

	private record StashResultAndTooltip(IStashStorageItem.StashResult stashResult,
										 Optional<TooltipComponent> tooltip) {
	}

	private static void onPlayerJoinServer(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		//noinspection ConstantConditions - by the time player is joining the world is not null
		RecipeHelper.setLevel(Minecraft.getInstance().level);
	}

}
