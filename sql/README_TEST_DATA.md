# 测试数据说明

## 文件说明

- `test_data_empty.sql` - 清空数据SQL，用于展示空状态界面
- `apply_test_data.bat` - 执行SQL的批处理脚本（Windows）

---

## 真实微信登录配置说明

系统已切换为真实微信登录模式，需要配置以下内容：

### 1. 服务端配置

在 `application.yml` 或环境变量中配置：

```yaml
app:
  wechat:
    app-id: your_miniprogram_appid      # 微信小程序AppID
    app-secret: your_miniprogram_secret  # 微信小程序AppSecret
```

或者设置环境变量：
- `WECHAT_APPID`
- `WECHAT_APPSECRET`

### 2. 客户端配置

在 `campus-errand-uniapp/src/manifest.json` 中配置：

```json
{
  "mp-weixin": {
    "appid": "your_miniprogram_appid"  // 填入微信小程序AppID
  }
}
```

### 3. 获取微信小程序配置

1. 登录 [微信公众平台](https://mp.weixin.qq.com)
2. 进入「开发」→「开发管理」→「开发设置」
3. 复制 AppID 和 AppSecret

### 4. 服务器域名白名单

在微信公众平台配置服务器域名：
- request合法域名：`https://your-domain.com`
- uploadFile合法域名：`https://your-domain.com`
- downloadFile合法域名：`https://your-domain.com`

## 空状态场景（5.2.x 和 5.3.x）

### 5.2.x 客户端空状态

| 场景 | 说明 | 测试用户 |
|------|------|----------|
| 5.2.1 | 钱包余额为空 | 测试发布者/测试跑腿员 |
| 5.2.2 | 任务大厅页面为空 | 无任务数据 |
| 5.2.3 | 任务发布金额为空 | 发布任务时金额为空（需手动测试） |
| 5.2.4 | 订单流转内我接单为空 | 测试跑腿员无接单 |
| 5.2.5 | 钱包截图与支付界面截图 | 测试发布者（需设置支付密码后测试） |

### 5.3.x 后台空状态

| 场景 | 说明 |
|------|------|
| 5.3.1 | 数据大屏为空 | 无业务数据 |
| 5.3.2 | 用户管理界面为空 | 仅保留2个测试用户 |
| 5.3.3 | 任务管理为空 | 无任务数据 |
| 5.3.4 | 钱包管理为空 | 无交易流水 |
| 5.3.5 | 评价为空 | 无评价数据 |

## 测试用户

| 用户类型 | 昵称 | OpenID |
|----------|------|--------|
| 发布者 | 测试发布者 | test_openid_publisher |
| 跑腿员 | 测试跑腿员 | test_openid_runner |

## 使用方法

### 方法1：使用批处理脚本（推荐）

双击运行 `sql/apply_test_data.bat`

### 方法2：手动执行SQL

```bash
# 进入MySQL
mysql -uroot -proot_password

# 使用数据库
USE campus_errand;

# 执行SQL脚本
source c:/Devs/CampusErrand/sql/test_data_empty.sql;
```

## 后续操作

1. 执行完SQL后，启动系统
2. 使用测试账号登录客户端
3. 截图记录各个空状态界面
4. 登录后台管理系统，截图后台空状态界面
