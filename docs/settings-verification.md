# 0.2.0 账号与设置：调研与验证

> 历史版本记录；当前实现以 `v0.4-verification.md` 为准。

日期：2026-11-08，设备仍为 peridot / Android 16 / HyperOS OS3.0。

## 官方实现依据

从已安装的钱包 APK（只读副本）解析 Manifest、DEX；进入官方钱包「设置」观察跳转页面，不修改配置，不提交支付或账号操作。

| 功能 | 官方钱包中的实现 | 素卡包采用方式 |
| --- | --- | --- |
| 小米账号 | 系统账号组件独立存在 | action `com.xiaomi.account.action.MI_ACCOUNT_SETTINGS`，固定到 `com.xiaomi.account` |
| 支付密码 | `com.xiaomi.jr.scaffold.LinkableActivity` 中的网页，`id=web.reset_password`，实际 URL 主路径为 `https://m.mipay.com/home/passManage` | 外部浏览器打开同一官方网页，去除钱包的追踪参数，保留 `from=webEntry` |
| 免密 / 自动扣款 | 同一网页容器，`id=mipay.autoPay.list`，URL 主路径为 `https://m.pay.xiaomi.com/deduct/recission` | 外部浏览器打开同一官方网页 |
| 钱包自身指纹 / 手势开关 | 钱包 `FingerprintSettingFragment` / `LockPatternSettingFragment`，分别处理 `app` 和 `card_folder`；容器 `MiuiStyleSettingActivity` 未导出 | 不直接调用或改写这些私有设置。改为提供系统应用锁管理入口，不能宣称与钱包的两个内部开关等价 |
| 智能卡设置 | 钱包 DEX 中包含 `https://tsmclient.miui.com?action=uni_settings&type=0&source_channel=wallet` | 显式调用智能卡 MainActivity，使用等价路由 `tsmclient://card?type=0&action=uni_settings` |

HyperOS 3 应用保护的可用入口为 `hyperos.intent.action.AUTH_USER_PRIVACY_APPS`，固定到 `com.miui.securitycenter`。真机进入 `com.miui.auth.BiometricAppLockSettingMainActivity` 并显示官方指纹 / 锁屏密码验证界面。旧入口 `miui.intent.action.APPLOCK_ACCESS_CONTROL` 在该手机上会立刻退出，因此只作为旧系统兼容候选；未宣称旧系统已验证。

浏览器从声明 `MAIN / APP_BROWSER` 的已安装应用中选取，并以包名固定打开 HTTPS URL，不把官方钱包作为候选或回退。如果没有浏览器就提示。未安装组件或外部调用被拒绝时显示错误。

## 验证边界

支付密码官方网页在外部浏览器中跳转到了小米账号登录页。系统账号登录不等同于浏览器登录；没有输入密码或提取官方应用的会话。**登录后的密码管理、免密/自动续费业务未验证，可能仍需要钱包网页桥接支持。**

系统应用锁只测试到官方身份验证页，未通过验证配置保护，也未修改任何指纹、锁屏密码、手势密码或应用锁开关。不会把打开设置视为保护已开启。

账号组件在当前已登录的手机上打开账号管理页；未退出账号测试首次登录。没有新增获取账号资料的权限。

首页原有四类卡片和刷卡路由未更改。应用新增私有 SettingsActivity；继续不声明任何 `uses-permission`，不使用联网、账号 API、后台服务或内嵌网页。
