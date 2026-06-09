# Fish-Chat UI 组件库开发计划

## 📐 整体架构设计

### 目录结构
```
webUI/src/components/ui/
├── styles/
│   ├── variables.css          # CSS变量系统（主题色、间距、圆角等）
│   ├── reset.css              # 样式重置
│   └── animations.css         # 全局动画定义
├── Button/
│   ├── Button.vue             # 按钮组件
│   └── Button.md              # 使用文档
├── Input/
│   ├── Input.vue              # 输入框组件
│   └── Input.md
├── Modal/
│   ├── Modal.vue              # 弹窗组件
│   └── Modal.md
├── Avatar/
│   ├── Avatar.vue             # 头像组件
│   └── Avatar.md
├── Card/
│   ├── Card.vue               # 卡片组件
│   └── Card.md
├── Badge/
│   ├── Badge.vue              # 徽章组件
│   └── Badge.md
├── Message/
│   ├── Message.vue            # 消息提示组件
│   ├── message.js             # 命令式API
│   └── Message.md
├── Skeleton/
│   ├── Skeleton.vue           # 骨架屏组件
│   └── Skeleton.md
└── index.js                   # 统一导出
```

## 🎨 设计规范

### 配色方案（清爽蓝色系）
```css
--primary-50: #eff6ff    /* 最浅蓝 - 背景 */
--primary-100: #dbeafe   /* 浅蓝 - 悬停 */
--primary-200: #bfdbfe
--primary-300: #93c5fd
--primary-400: #60a5fa   /* 主色 - 图标 */
--primary-500: #3b82f6   /* 主色 - 按钮 */
--primary-600: #2563eb   /* 主色 - 按下 */
--primary-700: #1d4ed8
--primary-800: #1e40af
--primary-900: #1e3a8a   /* 最深蓝 - 文字 */

--gray-50: #f9fafb
--gray-100: #f3f4f6
--gray-200: #e5e7eb
--gray-300: #d1d5db
--gray-400: #9ca3af
--gray-500: #6b7280
--gray-600: #4b5563
--gray-700: #374151
--gray-800: #1f2937
--gray-900: #111827

--success: #10b981
--warning: #f59e0b
--error: #ef4444
--info: #3b82f6
```

### 圆角规范
- 小圆角: `6px` (按钮、标签)
- 中圆角: `12px` (输入框、卡片)
- 大圆角: `16px` (弹窗、头像)
- 完全圆角: `9999px` (徽章、开关)

### 间距规范
- `4px` (超紧凑)
- `8px` (紧凑)
- `12px` (常规)
- `16px` (标准)
- `24px` (宽松)
- `32px` (超宽松)

### 阴影规范
- `sm`: `0 1px 2px 0 rgba(0, 0, 0, 0.05)`
- `md`: `0 4px 6px -1px rgba(0, 0, 0, 0.1)`
- `lg`: `0 10px 15px -3px rgba(0, 0, 0, 0.1)`
- `xl`: `0 20px 25px -5px rgba(0, 0, 0, 0.1)`

### 动画规范
- 快速: `150ms` (按钮、链接)
- 标准: `200ms` (卡片、输入框)
- 慢速: `300ms` (弹窗、侧边栏)
- 缓动函数: `cubic-bezier(0.4, 0, 0.2, 1)`

## 🧩 组件清单与优先级

### P0 - 核心组件（必须）
1. **Button** - 按钮
   - 类型: primary / secondary / outline / ghost / danger
   - 尺寸: sm / md / lg
   - 状态: default / hover / active / disabled / loading
   - 图标支持: 前缀/后缀图标

2. **Input** - 输入框
   - 类型: text / password / textarea / search
   - 状态: default / focus / error / disabled
   - 特性: 前缀/后缀图标、清除按钮、字符计数
   - 验证: 实时错误提示

3. **Modal** - 弹窗
   - 动画: 淡入 + 缩放
   - 特性: 遮罩层点击关闭、ESC关闭、拖拽移动
   - 尺寸: sm / md / lg / fullscreen
   - 头部: 标题 + 关闭按钮

4. **Avatar** - 头像
   - 形状: circle / square
   - 尺寸: xs / sm / md / lg / xl
   - 状态指示器: online / offline / busy
   -  fallback: 文字头像

5. **Message** - 消息提示
   - 类型: success / warning / error / info
   - 位置: top-center / top-right
   - 自动消失: 3秒
   - 命令式API: `Message.success('操作成功')`

### P1 - 常用组件（重要）
6. **Card** - 卡片
   - 样式: default / bordered / shadow
   - 组成部分: header / body / footer
   - 悬停效果

7. **Badge** - 徽章
   - 类型: dot / count
   - 位置: top-right / bottom-right
   - 颜色: primary / success / warning / error

8. **Skeleton** - 加载骨架屏
   - 形状: text / circle / rectangle
   - 动画: 渐变闪烁
   - 预设: avatar + text 组合

### P2 - 增强组件（可选）
9. **Tooltip** - 工具提示
10. **Dropdown** - 下拉菜单
11. **Tabs** - 标签页
12. **Empty** - 空状态

## 📝 开发规范

### 组件API设计原则
1. **Props**: 使用kebab-case (HTML风格)
2. **Events**: 使用kebab-case, 以`update:`前缀支持v-model
3. **Slots**: 命名使用kebab-case
4. **Composition API**: 使用`<script setup>`语法

### 样式编写规范
1. 使用CSS变量而非硬编码
2. Scoped样式 + BEM命名
3. 支持深色模式预留
4. 响应式设计（Mobile First）

### 组件导出规范
```javascript
// 统一导出
export { default as Button } from './Button/Button.vue'
export { default as Input } from './Input/Input.vue'
// ...

// 命令式组件
export { Message } from './Message/message.js'
```

## 🔄 实施步骤

### Phase 1: 基础建设 (1-2小时)
- [ ] 创建目录结构
- [ ] 定义CSS变量系统
- [ ] 创建全局动画
- [ ] 编写index.js导出

### Phase 2: 核心组件 (3-4小时)
- [ ] Button组件
- [ ] Input组件
- [ ] Modal组件
- [ ] Avatar组件
- [ ] Message组件

### Phase 3: 常用组件 (2-3小时)
- [ ] Card组件
- [ ] Badge组件
- [ ] Skeleton组件

### Phase 4: 应用集成 (1-2小时)
- [ ] 重构UserProfile页面
- [ ] 替换Element Plus依赖
- [ ] 优化视觉效果
- [ ] 测试响应式

## 🎯 最终效果

### UserProfile页面设计预览
- **主题色**: 清爽蓝色 (#3b82f6)
- **背景**: 纯白卡片 + 浅灰背景，去除花哨渐变
- **风格**: 现代简约，类似Twitter/Instagram个人主页
- **动画**: 微妙流畅，不过度设计
- **移动端**: 完美适配

### 视觉关键词
```
清爽 | 现代 | 简约 | 专业 | 流畅
```

## ⏱️ 预计时间
- **总计**: 7-11小时
- **最小可用**: 4-5小时（仅P0组件）
- **完整版本**: 7-11小时（P0+P1+集成）

## 📦 后续扩展
- 深色模式支持
- 国际化(i18n)
- 组件文档站点
- 单元测试
- 按需加载优化
