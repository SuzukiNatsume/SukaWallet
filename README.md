# 素卡包 · SukaWallet 0.8.0

依赖小米官方组件的极简开源卡包，包名为 `top.suzuki.sukawallet`。支持简体中文、繁体中文与英文，默认跟随系统。

## 界面

- 首页保留公交卡、门卡、Mi Pay 银行卡与车钥匙；Tap to Pay 固定在底部，列表独立滚动。
- 右上角三点菜单与设置页返回箭头使用同一尺寸规则，位置左右对称。
- 小米模式的设置页参照本机系统设置：纯黑深色背景、分组卡片、常规字重；返回图标使用本机系统设置的符号字体。
- 类原生模式采用 Pixel / Material 风格的动态色和分组列表。
- Appearance 和 App Protection 直接在 Settings 页内选择，使用右侧勾选标记的下拉菜单；仅设置或修改手势图案进入独立页面。
- 手势设置与验证共用一个正方形坐标区域，横纵点距一致；绘制和触摸判断使用同一组坐标。

两种风格均跟随系统深浅色。官方组件自己的页面外观和语言由官方决定。

## 官方服务

| 入口 | 接管方 |
| --- | --- |
| 公交卡、门卡、银行卡、车钥匙、刷卡 | 官方小米智能卡 `com.miui.tsmclient` |
| 小米账号 | 官方小米账号 `com.xiaomi.account` |
| 支付密码、免密 / 自动续费 | 官方账号组件的网页容器，使用系统账号的 `mipaycom` 服务登录态 |
| 智能卡设置 | 官方智能卡统一设置页 |

支付管理不打开官方钱包或外部浏览器。素卡包不读取、复制或保存官方账号令牌、Cookie 或卡片资料。无广告、无统计、无联网权限；只申请 `USE_BIOMETRIC`。

这是独立项目，与小米无隶属关系。此目录内原创源代码采用 [MIT](LICENSE)，不包含官方 APK、服务端或固件的开源授权。当前源码保存在本地，尚未发布到远程托管平台。

## 安装和保护

Android 11 或以上，并且当前 ROM 上的小米智能卡、账号组件本身能够正常工作。安装 `dist/SukaWallet-0.8.0.apk` 后，从右上角三点菜单进入 Settings。

App Protection 可选关闭、指纹 / 设备锁屏、手势图案。全新安装默认关闭；这台已连接手机原有的保护和风格配置会保留。

- 指纹 / 设备锁屏调用 Android 原生身份验证窗口，启用前先验证一次。
- 手势至少 4 个点，两次确认；只保存随机盐和 PBKDF2-HMAC-SHA256 派生值，连续 5 次错误暂缓 30 秒。
- 每个应用进程首次进入时验证一次。普通切后台、内部导航、从官方页面返回均不重复验证；进程被结束或系统回收后再次启动需要验证。
- 解锁状态仅存在进程内存，不写入持久存储。普通页面允许截图和录屏，手势页及尚未完成验证的页面保留截图保护。

本地保护只覆盖素卡包，不能限制从其他入口直接打开官方智能卡，也不会修改系统锁屏或官方钱包内部的密码。清除本应用数据会移除本地保护设置。

旧包 `dev.minicard.wallet` 的外观和保护配置已在本机通过同签名临时桥迁移；旧应用已停用并保留数据，没有卸载或清除。迁移桥源代码见 [tools/migration-bridge](tools/migration-bridge/README.md)。

## 构建

需要 JDK 17、Android SDK Platform 36、Build Tools 36.0.0 和系统 `zip`。使用原生 Java / View，无第三方应用库依赖，无需 Gradle。

```sh
export JAVA_HOME=/path/to/jdk17
export ANDROID_SDK_ROOT=/path/to/android-sdk
./build.sh
adb install -r dist/SukaWallet-0.8.0.apk
```

默认开发签名保存在 `.local/development.p12`，默认口令为 `android`；请保留用于覆盖升级，不应上传公开仓库。正式发布可通过 `MINICARD_KEYSTORE`、`MINICARD_KEY_ALIAS`、`MINICARD_STORE_PASSWORD` 使用自己的密钥。

## 验证和兼容范围

本机：peridot / Android 16 / HyperOS OS3.0。卡片与账号路由的既有验证见 [0.4.0 记录](docs/v0.4-verification.md)。未提交开卡、充值、修改支付密码或解除扣款协议等操作。

界面检查使用独立临时测试包，不改动正式应用的保护配置。测试工具见 [tests/render](tests/render/README.md)，本次结果见 [0.8.0 记录](docs/v0.8-verification.md)。

系统设置的图标资源和约 1.03125 倍密度适配按本机实测校准，不能据此保证其他手机也逐像素一致；缺少小米字体资源时使用内置矢量回退。官方内部路由也可能随组件更新变化。

双风格的组织方式参考 [KernelSU UiMode](https://github.com/tiann/KernelSU/blob/main/manager/app/src/main/java/me/weishu/kernelsu/ui/UiMode.kt)，下拉单选的勾选方式参考 [Miuix RadioButton](https://github.com/miuix-kotlin-multiplatform/miuix/blob/main/miuix-ui/src/commonMain/kotlin/top/yukonga/miuix/kmp/basic/RadioButton.kt)。Material 色彩来自 [Android 系统动态色](https://source.android.com/docs/core/display/material)。项目未引入 KernelSU 管理器代码、Miuix 或 Compose 库。
