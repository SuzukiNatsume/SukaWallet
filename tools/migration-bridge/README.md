# 旧包名设置迁移桥

此目录保存本次换包名时使用的临时 Provider 源码，用于审计与重现；不编入正式新应用。

它仅允许包名为 `top.suzuki.sukawallet` 且与旧应用签名相同的调用者读取 `appearance` / `protection` 两组偏好。没有导出账号、卡片或系统数据，也不允许写入。新应用确认导入后，手机上的旧应用已停用，保留原数据供恢复使用。

旧包：`dev.minicard.wallet`。临时 authority：`dev.minicard.wallet.migration`。需由旧包的同一签名密钥签署；仅复制此文件无法为任意应用获取访问权限。
