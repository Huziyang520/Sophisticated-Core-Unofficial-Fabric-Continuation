# 暂存（未参与编译）的兼容层源码

这里存放**本轮无法编译**的兼容实现，语法完整、逻辑未改，只是不参与构建。

## 为什么放在这里

26.3 Fabric 上，这些兼容模组要么**没有 26.3 版**，要么其 maven 仓库在本机网络下**无法获取编译期 API**：

| 兼容 | 停用原因 |
|---|---|
| EMI / Chipped / Trinkets / Curios / Quark / InventorySorter / ItemBorders / ModernKeyBinding | 26.3 无 Fabric 版 |
| Litematica（+ malilib） | 只有 CurseMaven 有，本机不可达 |
| AudioPlayer | 只有作者自建 maven / CurseMaven，本机不可达 |
| CraftingTweaks | 26.3 有 Fabric 版，但本机所有 maven 都取不到其 API 包 |

保留在源集里会直接编译失败，删除又会丢掉已写好的实现，因此移到 `src/parked/`（不在 Loom 的源集里）。

## 怎么接回来

1. 确认该模组的 API 已可作为编译期依赖解析（加进 `build.gradle` 的 `compileOnly`）。
2. 把对应的 `compat/<名字>/` 目录搬回 `src/main/java/net/p3pp3rf1y/sophisticatedcore/compat/`。
3. 在 `init/ModCompat.java` 的 `register()` 里补一行 `registry.registerCompat(...)`。
4. Litematica 还需把 `compat/litematica.mixins.json` 放回 `src/main/resources/compat/` 并写回 `fabric.mod.json` 的 `mixins` 列表。
