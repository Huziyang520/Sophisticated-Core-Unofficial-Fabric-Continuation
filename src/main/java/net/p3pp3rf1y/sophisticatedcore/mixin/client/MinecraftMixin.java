package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Client side hooks on {@link Minecraft}.
 * <p>
 * This mixin used to wrap {@code ParticleEngine.crack(BlockPos, Direction)} to suppress the block breaking particles
 * of storage blocks. 26.3 removed that method and rebuilt the particle engine around sprites, so the wrap is gone;
 * the shadowed level stays because the storage screens read it to resolve the current level.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public ClientLevel level;
}
