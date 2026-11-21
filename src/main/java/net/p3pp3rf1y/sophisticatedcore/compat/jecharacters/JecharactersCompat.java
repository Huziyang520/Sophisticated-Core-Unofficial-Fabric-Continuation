package net.p3pp3rf1y.sophisticatedcore.compat.jecharacters;

import net.fabricmc.loader.api.FabricLoader;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Optional bridge to Just Enough Characters' pinyin/initials matcher.
 * <p>
 * The mod ships no stable maven artifact, so the single static entry point is looked up reflectively once
 * and adapted to {@code (String, String) -> boolean}; without it every call falls back to a plain
 * case-insensitive substring test.
 */
public final class JecharactersCompat {
	private static final String MATCH_CLASS = "me.towdium.jecharacters.utils.Match";

	@Nullable
	private static final MethodHandle CONTAINS = findContains();

	private JecharactersCompat() {
	}

	@Nullable
	private static MethodHandle findContains() {
		if (!FabricLoader.getInstance().isModLoaded(CompatModIds.JECHARACTERS)) {
			return null;
		}
		try {
			Class<?> match = Class.forName(MATCH_CLASS);
			MethodHandle handle = MethodHandles.publicLookup().findStatic(match, "contains",
					MethodType.methodType(boolean.class, CharSequence.class, CharSequence.class));
			return handle.asType(MethodType.methodType(boolean.class, String.class, String.class));
		} catch (ReflectiveOperationException | LinkageError e) {
			return null;
		}
	}

	/**
	 * Matches a display name against one search term, using pinyin/initials when the mod is present.
	 */
	public static boolean contains(String text, String searchTerm) {
		if (CONTAINS != null) {
			try {
				return (boolean) CONTAINS.invokeExact(text, searchTerm);
			} catch (Throwable e) {
				// Fall through to the plain comparison so a broken optional dependency can't break search.
			}
		}
		return text.toLowerCase().contains(searchTerm.toLowerCase());
	}
}
