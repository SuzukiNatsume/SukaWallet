# 真机验证记录

> 历史版本记录；当前实现以 `v0.4-verification.md` 为准。

日期：2026-11-08（Asia/Taipei）。

设备：24069PC21G / peridot，Android 16，HyperOS OS3.0。
官方智能卡：`com.miui.tsmclient`，26.09.21.3.f。
应用：素卡包 0.1.0，`dev.minicard.wallet`。

## 已完成

- APK 编译、签名校验、USB 安装成功。
- 首页实际渲染正常，深色模式下四类卡片和刷卡按钮可见，无滚动遮挡。
- 安装包不声明任何 `uses-permission`；无网络库、统计 SDK 或后台服务。
- 从本应用首页实际点击各按钮，核对前台官方 Activity：

| 按钮 | 路由 | 实际页面 |
| --- | --- | --- |
| 公交卡 | `tsmclient://card?type=TRANSCARD&action=transit_detail` | `ui.IssuedTransCardListActivity` |
| 门卡 | `tsmclient://card?type=MIFARE_ENTRANCE&action=issue` | `ui.MifareCardListActivity` |
| 银行卡 | `tsmclient://card?type=BANKCARD&action=mipay_list` | `ui.MiPayEntranceActivity` |
| 车钥匙 | `tsmclient://card?type=DIGITALKEY&action=issue` | 先进入 `ui.digitalkey.CarKeyListActivity`，随后自动进入 `ui.digitalkey.CarKeyOEMListActivity` 品牌选择页 |
| 刷卡 | `tsmclient://swiping` | `ui.quick.DoubleClickActivity` |

四类管理入口固定调用官方 `ui.MainActivity`；刷卡入口固定调用官方 `ui.quick.DoubleClickActivity`。这些是官方内部路由，并非稳定公开 API。

## 验证边界

只验证应用安装、首页显示及官方页面跳转。没有提交开卡、充值、复制门卡、绑定银行卡或配对车钥匙，没有验证真实读卡器/POS/车辆上的使用结果，没有删除或更改任何已有卡片，也没有禁用官方组件。

未实测其他智能卡版本、其他机型、缺少官方组件的 ROM、系统拒绝调用的情形、浅色模式和超大字体布局。浅色主题及失败提示已实现，不能据此称为已完成真机验证。
