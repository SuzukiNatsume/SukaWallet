# 真机渲染检查

此测试使用独立包名 `top.suzuki.sukawallet.rendercheck`，不导入正式应用的账号或保护配置，不含桌面启动入口。只在临时版本中使用验证模式测试数据，正式 APK 不包含这些代码。

## 使用

准备与正式构建相同的 JDK / SDK 环境后：

```sh
python3 tests/render/build_preview.py
# 上一步会输出临时构建目录；进入该目录执行 build.sh，再安装 dist 中的测试 APK。
python3 tests/render/run_matrix.py
adb uninstall top.suzuki.sukawallet.rendercheck
```

运行矩阵会暂时更改设备 DPI、字体缩放和动画倍率。正常结束或 Python 异常退出时，通过 finally 恢复原配置；强行结束进程或断开设备可能阻止恢复，应先记下原配置。当前已验证设备原配置是 384 DPI、font_scale=0.9。

## 覆盖

- 320 / 384 / 420 / 480 DPI。
- 85 / 90 / 100 / 110 / 115 / 125 / 130 / 150 / 180 / 200% 字体缩放。
- 小米、Material 两种风格。
- 首页、设置、手势设置和手势验证；设置额外覆盖已启用手势后的“更改图案”行。
- 两种下拉选项的弹出内容、普通页面和手势页的截图策略。
- 480 DPI / 200% 下额外检查简体中文和繁体中文。

测试直接在手机显示真实 Activity / PopupWindow，检查 TextView 布局裁切、ellipsis、横向与纵向越界。85% 与 200% 等极端字号的布局检查不能替代所有设备和系统字体下的视觉检查。

手势流程单独执行：

```sh
adb shell am instrument -w -e gestures true top.suzuki.sukawallet.rendercheck/top.suzuki.sukawallet.RenderChecks
```

此流程通过 Activity.dispatchTouchEvent 分发触摸，验证短图案、确认不匹配、保存及错误/正确解锁。仅清理诊断包的保护配置。
