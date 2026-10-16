# Sophisticated Core (Unofficial Fabric Continuation)

A shared library providing the storage, upgrade, settings, GUI and rendering framework used by Sophisticated Storage and Sophisticated Backpacks.

## What it does

A shared library mod. It is the common code base of **Sophisticated Storage** and
**Sophisticated Backpacks** and provides everything those two mods build on:

* the storage wrapper / inventory framework shared by block containers and backpacks;
* the upgrade framework (pickup, magnet, filter, void, feeding, compacting, cooking, crafting,
  stonecutter, jukebox, stack, tank, pump, experience pump, battery, infinity …);
* container settings (main, memory, no-sort, item display) including datapack-provided templates;
* the container screen: scrolling inventory, search box, sorting, colour and item-display settings;
* the multi-block storage controller framework;
* fluid handling and the dynamic fluid-container model.

## Requirements

* Minecraft (Fabric)
* [Fabric API](https://modrinth.com/mod/fabric-api)
* [Cloth Config API](https://modrinth.com/mod/cloth-config) — required, it provides the in-game
  configuration screen
* [Mod Menu](https://modrinth.com/mod/modmenu) — optional, adds the config button to the mod list

## License and credits

This repository is a **continuation of the unofficial Fabric port of Sophisticated Core**. It is a fork of the
GPL-licensed branch maintained by **Salandora**, and it is released under the **same license,
GNU General Public License v3.0** — see [LICENSE](LICENSE). The original copyright notices and the
license text are kept unchanged.

* **Sophisticated Core** — original mod and original author: **P3pp3rF1y** (all original
  design, code and assets).
* **Sophisticated Core (unofficial Fabric port)** — **Salandora**, whose GPL v3 branch this
  continuation builds upon.
* **This continuation** — Huziyang520.

We are grateful to P3pp3rF1y and to Salandora for creating and porting these mods, and for releasing
their work under a license that allows this continuation to exist. Like the original authors, we owe
the Minecraft modding community a debt of thanks: the mapping and modloader teams, the authors of the
mods we integrate with, and everyone who reports bugs and suggests improvements make work like this
possible.

## Links

* Sources, issues and feature requests: <https://github.com/Huziyang520/Sophisticated-Core-Unofficial-Fabric-Continuation>
* Original mod: <https://www.curseforge.com/minecraft/mc-mods/sophisticated-core>
* Unofficial Fabric port this continuation is based on: <https://github.com/Salandora/SophisticatedCore>

---

# Sophisticated Core（非官方 Fabric 移植续）

为精妙存储与精妙背包提供存储、升级、设置、界面与渲染框架的共用前置库。

## 功能简介

共用前置库，是**精妙存储**与**精妙背包**的公共代码基座，提供：

* 方块容器与背包共用的存储包装 / 库存框架；
* 升级框架（拾取、磁吸、过滤、丢弃、喂食、压缩、烹饪、工作台、切石机、唱片机、堆叠、储罐、
  泵、经验泵、电池、无限……）；
* 容器设置（主设置、记忆槽、不排序槽、物品展示），支持数据包提供设置模板；
* 容器界面：可滚动库存、搜索框、排序、颜色与物品展示设置；
* 多方块存储控制器框架；
* 流体处理与动态流体容器模型。

## 前置

* Minecraft（Fabric）
* [Fabric API](https://modrinth.com/mod/fabric-api)
* [Cloth Config API](https://modrinth.com/mod/cloth-config) —— 必选，提供游戏内配置界面
* [Mod Menu](https://modrinth.com/mod/modmenu) —— 可选，在模组列表加入配置按钮

## 许可证与致谢

本仓库是**Sophisticated Core 非官方 Fabric 移植版**的续作，fork 自 **Salandora** 维护的 GPL 分支，并以**相同
许可证 GNU General Public License v3.0** 开源（见 [LICENSE](LICENSE)），原版权声明与许可证正文原样保留。

* **Sophisticated Core** —— 原模组与原作者：**P3pp3rF1y**（全部原始设计、代码与美术）。
* **Sophisticated Core（非官方 Fabric 移植版）** —— **Salandora**，本续作所依据的 GPL v3 分支作者。
* **本续作** —— Huziyang520。

感谢 P3pp3rF1y 创造了这些模组、感谢 Salandora 完成了 Fabric 移植，并感谢他们以允许二次开发的许可证
发布作品。与原作者的看法一致，我们也同样感谢模组社区：映射表与加载器团队、我们联动过的各个模组的
作者，以及所有反馈问题、提出建议的玩家，正是他们让这类工作成为可能。

## 链接

* 源码 / 问题反馈 / 功能建议：<https://github.com/Huziyang520/Sophisticated-Core-Unofficial-Fabric-Continuation>
* 原模组：<https://www.curseforge.com/minecraft/mc-mods/sophisticated-core>
* 本续作所依据的非官方 Fabric 移植版：<https://github.com/Salandora/SophisticatedCore>
