package net.p3pp3rf1y.sophisticatedcore.init;

import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.JeiCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.rei.REICompat;

import static net.p3pp3rf1y.sophisticatedcore.SophisticatedCore.MOD_ID;

public class ModCompat {
	private ModCompat() {
	}

	/**
	 * 只注册 26.3 上确实拿得到公开 API 的兼容。
	 * 其余兼容（EMI / Chipped / Curios / Trinkets / Litematica / AudioPlayer / InventorySorter / ItemBorders /
	 * CraftingTweaks）的源码已移到 <code>src/parked/java</code>：它们或没有 26.3 Fabric 版，或其 maven 在本机不可达，
	 * 无法作为编译期依赖。等上游有版本、依赖可解析时，把对应包搬回 <code>src/main/java</code> 并在这里补一行注册即可。
	 */
	public static void register() {
		CompatRegistry registry = CompatRegistry.getRegistry(MOD_ID);
		registry.registerCompat(new CompatInfo(CompatModIds.JEI, null), () -> new JeiCompat());
		registry.registerCompat(new CompatInfo(CompatModIds.REI, null), () -> new REICompat());
	}
}
