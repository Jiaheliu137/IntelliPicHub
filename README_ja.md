# IntelliPicHub

<div align="center">

🌐 [English](README.md) | [中文](README_zh.md) | **日本語**

</div>

## 概要

IntelliPicHub は多機能な画像表示プラットフォームです

- **パブリックスペース**：素材サイトや壁紙サイトとして使用可能
- **プライベートスペース**：個人アルバムや個人作品集として使用可能
- **チームスペース**：メンバーを招待して画像や素材を共有可能

**[オンラインで試す](https://picture.jiaheliu.top/)**

## 機能

### AI画像拡張
さらに、画像に対してAI拡張を行うことができます

<img src="./README.assets/image-20250527070253144.png" alt="AI画像拡張機能" style="zoom:50%;" />

### 逆画像検索
逆画像検索（類似画像の検索）

<img src="./README.assets/image-20250527072120361.png" alt="逆画像検索" style="zoom:50%;" />

### マルチユーザーリアルタイム協調編集
チームスペースでは、画像に対してマルチユーザーリアルタイム協調編集を行うことができます

![マルチユーザー協調編集デモ](./README.assets/demo.gif)

## デプロイメントガイド

### 環境要件

システムに以下の環境がインストールされている必要があります：
- MySQL
- Redis
- Java 17
- Maven

### 1. プロジェクトのクローン

```bash
git clone https://github.com/Jiaheliu137/IntelliPicHub
cd IntelliPicHub
```

### 2. バックエンドの設定

#### 2.1 設定ファイルのコピー

```bash
cd IntelliPicHub-backend/src/main/resources
cp application.example.yml application-local.yml
```

#### 2.2 設定情報の入力

プロンプトに従って `application-local.yml` の設定を入力してください

#### 2.3 Tencent Cloudストレージの設定

[Tencent Cloudストレージバケット](https://console.cloud.tencent.com/cos/bucket)から以下の設定情報を取得してください：

```yaml
cos:
  client:
    host: your_cos_host
    secretID: your_cos_secret_id
    secretKey: your_cos_secret_key
    region: your_cos_region
    bucket: your_cos_bucket_name
```

そして[データ万象サービス](https://console.cloud.tencent.com/ci)を有効にしてください

### 3. フロントエンドの設定

```bash
cd IntelliPicHub-frontend/src/config
```

`index.ts` ファイル内の `const PROD_DOMAIN = ''` を自分のドメインに変更してください

### 4. バックエンドの起動

```bash
cd IntelliPicHub-backend
mvn clean package -DskipTests
java -jar target/IntelliPicHub-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### 5. フロントエンドのビルド

```bash
cd IntelliPicHub-frontend
npm run build-only
```

### 6. Nginx設定

Nginx設定の参考例です。ドメインとフロントエンドビルドパスを自分のものに置き換えてください：

```nginx
# IntelliPicHub
server {
    listen 80;
    listen 443 ssl http2;
    server_name picture.jiaheliu.top;

    ssl_certificate     /etc/nginx/save_cert_key/wildcard/cert.pem;
    ssl_certificate_key /etc/nginx/save_cert_key/wildcard/key.pem;

    if ($scheme = http) {
        return 301 https://$host$request_uri;
    }

    location / {
        root /root/github_project/IntelliPicHub/IntelliPicHub-frontend/dist;
        index index.html;
        try_files $uri $uri/index.html /index.html;
    }

    location /api {
        proxy_pass http://localhost:8123/api;
        proxy_set_header Host $proxy_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Connection "";
        proxy_buffering off;
    }
    
    location /api/ws {
        proxy_pass http://localhost:8123/api/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $proxy_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
        proxy_buffering off;
    }
} 