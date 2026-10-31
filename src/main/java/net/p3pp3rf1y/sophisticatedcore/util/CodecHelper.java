package net.p3pp3rf1y.sophisticatedcore.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class CodecHelper {
	/**
	 * Item stack codec that keeps counts above the vanilla stack limit.
	 * <p>
	 * 26.3 no longer exposes {@code ITEM_NON_AIR_CODEC}, so the item holder codec is used directly.
	 */
	public static final Codec<ItemStack> OVERSIZED_ITEM_STACK_CODEC = Codec.lazyInitialized(
			() -> RecordCodecBuilder.create(
					instance -> instance.group(
									BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStack::typeHolder),
									Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
									DataComponentPatch.CODEC
											.optionalFieldOf("components", DataComponentPatch.EMPTY)
											.forGetter(stack -> stack.getComponentsPatch())
							)
							.apply(instance, ItemStack::new)));

	public static final PrimitiveCodec<Integer> STRING_ENCODED_INT = new PrimitiveCodec<Integer>() {
		@Override
		public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
			return ops.getStringValue(input).map(s -> {
				if (s.startsWith("i")) {
					return Integer.parseInt(s.substring(1));
				} else {
					return Integer.parseInt(s);
				}
			});
		}

		@Override
		public <T> T write(final DynamicOps<T> ops, final Integer value) {
			return ops.createString("i" + value);
		}

		@Override
		public String toString() {
			return "Int";
		}
	};

	private CodecHelper() {}

	public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
		return new SetCodec<>(elementCodec);
	}
}
