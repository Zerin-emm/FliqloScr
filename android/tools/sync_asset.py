#!/usr/bin/env python3
"""把 web/fliqlo.html 同步成 Android 工程的 assets/index.html。

为什么需要这个脚本：assets 里的副本要带几处「App 环境专用」的小改，
手工复制会漏。以后改了 web/fliqlo.html，重跑一次这个脚本即可：

    python android/tools/sync_asset.py
"""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "web" / "fliqlo.html"
DST = ROOT / "android" / "app" / "src" / "main" / "assets" / "index.html"

# (原文, 替换成, 说明)
PATCHES = [
    (
        'getLocalStorage("br");isNaN(e)?',
        'getLocalStorage("br");null==e&&(e=NaN),isNaN(e)?',
        "首次运行 getItem 返回 null，isNaN(null) 为 false，会把亮度错误地夹到 50",
    ),
    (
        'getLocalStorage("sc");isNaN(t)?',
        'getLocalStorage("sc");null==t&&(t=NaN),isNaN(t)?',
        "同上：缩放也会被错误地夹到 50，手机竖屏下时钟只有一半大",
    ),
]


def main() -> None:
    html = SRC.read_text(encoding="utf-8")
    for old, new, why in PATCHES:
        if new in html:
            continue
        count = html.count(old)
        if count != 1:
            raise SystemExit(f"PATCH 匹配失败（{count} 处）：{why}\n原文片段：{old}")
        html = html.replace(old, new, 1)

    DST.parent.mkdir(parents=True, exist_ok=True)
    DST.write_text(html, encoding="utf-8")
    print(f"已写入 {DST}（{len(html)} 字符，{DST.stat().st_size} 字节）")


if __name__ == "__main__":
    main()
