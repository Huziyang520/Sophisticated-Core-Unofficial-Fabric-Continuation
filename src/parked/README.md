# 暂存（不参与编译）的源码

这里的代码**语法保留、逻辑未改**，只是当前无法编译，所以不放进 `src/main/java`。
需要时搬回 `src/main/java` 对应包下即可恢复编译。

## 为什么放在这里

| 内容 | 停用原因 |
|---|---|
| `compat/emi`、`compat/chipped`、`compat/trinkets`、`compat/curios`、`compat/inventorysorter`、`compat/itemborders` | 这些模组在 26.3 没有 Fabric 版，编译期 API 不存在 |
| `compat/audioplayer` | AudioPlayer 26.3 版存在，但其 API jar 的坐标尚未核实，暂缓 |
| `client/render/CompositeModel`、`util/model/DynamicFluidContainerModel` | 26.3 删除了 `BakedModel`/`MultiBufferSource`/`ItemOverrides`/`IGeometryBakingContext` 这一整代模型 API，需按新的 `UnbakedModel`/`QuadCollection` 体系重写 |

## 怎么接回来

1. 确认依赖能作为编译期坐标解析（`build.gradle` 的 `modCompileOnly`）。
2. 把目录搬回 `src/main/java/net/p3pp3rf1y/sophisticatedcore/` 下同名包。
3. 在 `init/ModCompat.register()` 里补一行注册。
4. Litematica 相关的 mixin 配置在 `src/main/resources/compat/litematica.mixins.json`，已保留。
