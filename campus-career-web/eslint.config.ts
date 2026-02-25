import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import pluginOxlint from 'eslint-plugin-oxlint'
import skipFormatting from 'eslint-config-prettier/flat'

/**
 * ESLint 配置（Flat Config）：
 * - 约束 Vue + TypeScript 代码风格与常见问题
 * - 与 `npm run lint:eslint` 脚本配合使用（会自动 --fix）
 * - 同时读取 `.oxlintrc.json`，让 oxlint 的规则体系参与校验
 *
 * 可选项说明：
 * - 如果团队不使用 ESLint/Oxlint，可删除本文件与 `.oxlintrc.json`，
 *   并从 `package.json` 移除相关脚本与依赖（不推荐，除非你们有替代方案）。
 */
// To allow more languages other than `ts` in `.vue` files, uncomment the following lines:
// import { configureVueProject } from '@vue/eslint-config-typescript'
// configureVueProject({ scriptLangs: ['ts', 'tsx'] })
// More info at https://github.com/vuejs/eslint-config-typescript/#advanced-setup

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{vue,ts,mts,tsx}'],
  },

  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  ...pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,

  ...pluginOxlint.buildFromOxlintConfigFile('.oxlintrc.json'),

  skipFormatting,
)
