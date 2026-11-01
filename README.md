# Fliqlo 网页版

[**Windows**](/windows/README.md) | **总览** | [**Android**](/android/README.md)


从 **Fliqlo 1.6.1 for Windows** 的 `Fliqlo.scr` 里提取的翻页时钟页面，并补上了显秒和设置栏。
一份页面，两种形态：Windows 上双击用浏览器全屏当屏保，Android 上装成原生 App 当床头钟。

## 概览

### 两个平台

| 平台 | 产物 | 构建方式 | 说明 |
|---|---|---|---|
| Windows | `windows/Output/Fliqlo-Setup-*.exe` | Inno Setup 编译 `windows/Fliqlo.iss` | [windows/README.md](windows/README.md) |
| Android | `Fliqlo-release.apk`（约 0.65 MB） | Gradle / Android Studio | [android/README.md](android/README.md) |

两个平台共用同一份页面 `web/fliqlo.html`，差别只在打包方式：Windows 用浏览器打开它，
Android 把它打进 APK 的 assets 交给 WebView 加载。所以**改页面只需要改一个文件**。

### 目录结构

规则只有一条：**`web/` 放两个平台共享的页面源，每个平台一个目录放自己的构建输入、文档和产物。**

```
FliqloScr/
├─ README.md                本文件：项目总览
├─ docs/                    screenshot.png（页面实拍图）
├─ Fliqlo.ico               图标源（Windows 直接用；Android 的图标由它转出 PNG）
├─ web/
│  └─ fliqlo.html           唯一可编辑源，两个平台都从这里取
├─ windows/
│  ├─ README.md             Windows 版说明
│  ├─ Fliqlo.iss            安装包脚本，引用 ..\web\fliqlo.html 与 ..\Fliqlo.ico
│  └─ Output/               编译产物（Fliqlo-Setup-*.exe）
└─ android/
   ├─ README.md             Android 版说明
   ├─ docs/                 竖屏 / 横屏渲染预览图
   ├─ app/                  标准 Gradle 工程
   └─ tools/                页面同步脚本
```

**文档图片的放法**：每个 README 用自己旁边的 `docs/`，相对路径引用（`docs/xxx.png`），
不搞根目录统一存放。这样任何一份 README 连同配图被单独复制或拆成独立仓库时都不会断图。
也不用 `assets/` 这个名字——Android 的 `app/src/main/assets/` 是打包进 APK 的运行时资源，
同名不同义，容易误伤。


### 生成物

`android/` 里有**从 `web/` 生成的副本**，它不是第二份真源：

| 生成物 | 由谁生成 |
|---|---|
| `android/app/src/main/assets/index.html` | `python android/tools/sync_asset.py` |
| `android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` | 手工从 `Fliqlo.ico` 提取的 256px 图层 |

`windows/` 不存在副本：`.iss` 安装时直接引用 `..\web\fliqlo.html`（`Source: "..\web\{#MyAppFileName}"`），
所以 Windows 包不用同步。

## 功能

- **时:分 / 时:分:秒 两种形态**：小时格式下拉里的 `+S` 三项就是显秒模式
- **设置栏常驻可用、自动隐藏**：鼠标移动时出现在屏幕底部，停 3 秒淡出
- 三种小时写法（`1-12 AM/PM` / `00-23` / `0-23`）、AM/PM 角标、翻片底板、缩放 50–100、亮度 50–100
- 横竖屏自动重排；三卡模式在 16:9 屏上整体缩到约 82%
- 零外部请求：字体是 base64 内嵌，CSS/JS 全在一个文件里，不联网
- 翻页动画沿用原版：165ms 的 CSS 3D 翻转

![实际效果：1600×900 视口下的渲染](docs/screenshot.png)

## 实现要点

### 对原版的三处适配

原版页面为 WebView2 宿主设计，靠 `window.chrome.webview.postMessage` 与宿主通信；
在普通浏览器里运行需要做三处适配，文件中分别以 `PATCH1` / `PATCH2` / `PATCH3` 标注，
升级原版时搜索这三个词即可定位：

- **PATCH1**：宿主检测改为「存在 `chrome.webview` 才视为 WebView2 宿主」，
  浏览器环境因此走浏览器自己的 `localStorage`
- **PATCH2**：与宿主通信的 `postMessage` / `addEventListener` 加存在性检查，
  在无宿主的环境下静默跳过
- **PATCH3**：设置栏常开——浏览器里没有宿主的「鼠标不动才显示」事件

### 显秒是怎么做的

原版只有「时 / 分」两张卡片。这里加了第三张 `.flips#secs`，布局规则全部挂在 `body.secs-on` 下，
只在该状态下把容器从 1350×700 扩到 1950×700（竖屏 700×1950）。

小时格式下拉从 3 项扩成 6 项，选中 `+S` 时给 `body` 加类、广播 `_resize` 重算缩放；
秒卡由一段独立脚本按 175ms 定时器驱动，复用原版 `act/transform` 与 `fadein/fadeout1` 的 class 流程，
完全不碰原版时/分逻辑。切换格式后会把 `dsptf` 置空，强制小时卡立即按新格式重绘——
原版这里要等到下一个整点才刷新。

## 设置项

| key | 含义 | 取值 |
|---|---|---|
| `tf` | 时间格式 | `0`=1-12 AM/PM，`1`=00-23，`2`=0-23；下拉框的 `3/4/5` 是对应的 `+S` |
| `bg` | 翻片底板 | `1` / `0`（旧版本存的 `true` / `false` 仍能正确读取） |
| `br` | 亮度 | 50–100 |
| `sc` | 缩放 | 50–100 |
| `ss` | 显秒 | `1` / `0` |

存储位置是浏览器的 `localStorage`，按页面来源隔离（`file://` 与 `http://localhost` 是两套）。
和原版 `.scr` 用的 `%LOCALAPPDATA%\Fliqlo\webview-storage.json` **互不相通**。

## 注意

- 网页无法阻止系统按电源计划关闭显示器或进入睡眠；要常亮请把“关闭屏幕/睡眠”设为“从不”
- 这是非官方重打包：页面与字体的版权归原作者 Yuji Adachi / 9031（(https://fliqlo.com/)