package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/**
 * Builds a serializer for a recipe that wraps another recipe.
 * <p>
 * 26.3 made {@link RecipeSerializer} a record, so this is a factory returning an instance
 * instead of a subclass.
 */
public final class RecipeWrapperSerializer {
	private RecipeWrapperSerializer() {
	}

	public static <T extends Recipe<?>, R extends Recipe<?> & IWrapperRecipe<T>> RecipeSerializer<R> create(
			Function<T, R> initialize, RecipeSerializer<T> recipeSerializer) {
		MapCodec<R> codec = recipeSerializer.codec().xmap(initialize, IWrapperRecipe::getCompose);
		StreamCodec<RegistryFriendlyByteBuf, R> streamCodec = new StreamCodec<>() {
			@Override
			public R decode(RegistryFriendlyByteBuf buffer) {
				T compose = recipeSerializer.streamCodec().decode(buffer);
				return compose == null ? null : initialize.apply(compose);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buffer, R value) {
				recipeSerializer.streamCodec().encode(buffer, value.getCompose());
			}
		};
		return new RecipeSerializer<>(codec, streamCodec);
	}
}
