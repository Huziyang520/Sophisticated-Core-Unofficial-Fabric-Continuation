package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/**
 * Builds a {@link RecipeSerializer} for a recipe that wraps another one.
 * <p>
 * 26.3 turned {@code RecipeSerializer} into a record of a {@link MapCodec} and a {@link StreamCodec}, so a wrapper
 * can no longer be a serializer itself - it produces one instead, mapping the wrapped recipe onto the wrapping type
 * on the way in and back out again.
 */
public final class RecipeWrapperSerializer {
	private RecipeWrapperSerializer() {
	}

	/** @return a serializer for {@code R} that reads and writes it as {@code T} passed through {@code initialize}. */
	public static <T extends Recipe<?>, R extends Recipe<?> & IWrapperRecipe<T>> RecipeSerializer<R> create(
			Function<T, R> initialize, RecipeSerializer<T> recipeSerializer) {
		MapCodec<T> wrappedCodec = recipeSerializer.codec();
		StreamCodec<RegistryFriendlyByteBuf, T> wrappedStreamCodec = recipeSerializer.streamCodec();
		MapCodec<R> codec = wrappedCodec.xmap(initialize, IWrapperRecipe::getCompose);
		StreamCodec<RegistryFriendlyByteBuf, R> streamCodec = new StreamCodec<>() {
			@Override
			public R decode(RegistryFriendlyByteBuf buffer) {
				T compose = wrappedStreamCodec.decode(buffer);
				return compose == null ? null : initialize.apply(compose);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buffer, R value) {
				wrappedStreamCodec.encode(buffer, value.getCompose());
			}
		};
		return new RecipeSerializer<>(codec, streamCodec);
	}

	/**
	 * @return a serializer for a recipe that has no fields of its own, so nothing needs to be read or written.
	 * This is what vanilla's removed {@code SimpleCraftingRecipeSerializer} did.
	 */
	public static <T extends Recipe<?>> RecipeSerializer<T> unit(T instance) {
		return new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance));
	}
}
