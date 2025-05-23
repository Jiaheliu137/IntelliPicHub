
// Production environment domain name configuration - modify to your own domain name when deploying |
// 生产环境域名配置 - 部署时修改为自己的域名
const PROD_DOMAIN = 'picture.jiaheliu.top'

// 开发环境配置
const DEV_HTTP_URL = 'http://localhost:8123'
const DEV_WS_URL = 'ws://localhost:8123'

// 生产环境配置
const PROD_HTTP_URL = `https://${PROD_DOMAIN}`
const PROD_WS_URL = `wss://${PROD_DOMAIN}`

export {
  PROD_DOMAIN,
  DEV_HTTP_URL,
  DEV_WS_URL,
  PROD_HTTP_URL,
  PROD_WS_URL
}
