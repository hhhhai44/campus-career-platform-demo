# campus-career-web

基于 **Vue 3 + Vite + TypeScript** 的前端工程模板，内置：
- Vue Router（路由）
- Pinia（状态管理）
- ESLint/Prettier/Oxlint（代码质量与格式化）

更多“每个文件/文件夹的作用”和“哪些可删除”的说明见：
- `docs/项目结构说明.md`

## 环境要求

- Node.js：`package.json` 中已声明 engines（建议使用 Node 20.19+ 或 22.12+）
- 包管理：npm

## 安装依赖

```sh
npm install
```

## 本地开发（热更新）

```sh
npm run dev
```

### 联调后端登录接口（重要）

本前端默认通过 Vite 代理把 `/api/**` 转发到后端：
- 后端默认端口：`8081`
- 登录接口：`POST /auth/login`
- 前端调用地址：`POST /api/auth/login`（开发期会被代理到 `http://localhost:8081/auth/login`）

## 构建（生产）

```sh
npm run build
```

## 类型检查

```sh
npm run type-check
```

## 代码规范（自动修复）

```sh
npm run lint
```

## 格式化

```sh
npm run format
```
