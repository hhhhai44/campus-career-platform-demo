<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { qwenAskStream } from '@/api/qwen'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'

const MAX_QUESTION_LEN = 2000

type ChatItem = {
  role: 'user' | 'assistant' | 'system'
  content: string
  ts: number
}

const question = ref('')
const loading = ref(false)
const errorText = ref<string | null>(null)
const abortController = ref<AbortController | null>(null)
const chatRef = ref<HTMLElement | null>(null)

const items = ref<ChatItem[]>([
  {
    role: 'system',
    content:
      '嗨，我是学涯助手。你可以聊学习规划、实习求职和职业方向，我会给你可执行的建议。',
    ts: Date.now(),
  },
])

const questionLength = computed(() => question.value.trim().length)
const canSend = computed(() => questionLength.value > 0 && !loading.value)

const md = new MarkdownIt({
  breaks: true,
  linkify: true,
})

async function onSend() {
  const q = question.value.trim()
  if (!q || loading.value) return

  errorText.value = null
  items.value.push({ role: 'user', content: q, ts: Date.now() })
  await scrollChatToBottom()
  question.value = ''

  const assistantItem: ChatItem = {
    role: 'assistant',
    content: '',
    ts: Date.now(),
  }
  items.value.push(assistantItem)
  const assistantIndex = items.value.length - 1
  await scrollChatToBottom()

  const controller = new AbortController()
  abortController.value = controller
  loading.value = true

  try {
    await qwenAskStream(q, {
      signal: controller.signal,
      onDelta: (delta) => {
        const current = items.value[assistantIndex]
        if (!current) return
        setAssistantContent(assistantIndex, `${current.content}${delta}`)
        void scrollChatToBottom()
      },
      onError: (msg) => {
        errorText.value = msg
      },
    })

    if (!items.value[assistantIndex]?.content.trim()) {
      setAssistantContent(assistantIndex, '(空响应)')
    }
  } catch (e: any) {
    if (e?.name === 'AbortError') {
      if (!items.value[assistantIndex]?.content.trim()) {
        setAssistantContent(assistantIndex, '(已停止生成)')
      }
      return
    }

    const msg = e?.message || '请求失败'
    errorText.value = msg
    if (!items.value[assistantIndex]?.content.trim()) {
      setAssistantContent(
        assistantIndex,
        `【暂时无法回答】${msg}\n\n请稍后再试，或联系管理员检查助手服务配置。`,
      )
    }
  } finally {
    abortController.value = null
    loading.value = false
    await scrollChatToBottom()
  }
}

function setAssistantContent(index: number, content: string) {
  const current = items.value[index]
  if (!current) return
  items.value[index] = {
    role: current.role,
    ts: current.ts,
    content,
  }
}

function onStop() {
  abortController.value?.abort()
}

function onClear() {
  onStop()
  items.value = items.value.filter((x) => x.role === 'system')
  errorText.value = null
}

async function scrollChatToBottom() {
  await nextTick()
  const el = chatRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}

function renderMarkdown(content: string) {
  const normalized = normalizeAssistantMarkdown(content || '')
  const html = md.render(normalized)
  return DOMPurify.sanitize(html)
}

function normalizeAssistantMarkdown(input: string) {
  let text = cleanVisibleContent(input.replace(/\r\n/g, '\n'))

  // Fix headings without a space: ###标题 -> ### 标题
  text = text.replace(/^(#{1,6})([^\s#])/gm, '$1 $2')

  // Fix numbered lists without a space: 1.内容 -> 1. 内容
  text = text.replace(/^(\d+)\.([^\s])/gm, '$1. $2')

  // Fix bullet lists without a space: -内容 -> - 内容
  text = text.replace(/^([*-])([^\s])/gm, '$1 $2')

  // Ensure a blank line before headings/lists so markdown parser can split sections.
  text = text.replace(/([^\n])\n(#{1,6}\s)/g, '$1\n\n$2')
  text = text.replace(/([^\n])\n((?:\d+\.\s|[-*]\s))/g, '$1\n\n$2')

  // Collapse excessive blank lines.
  text = text.replace(/\n{3,}/g, '\n\n')
  return text.trim()
}

function cleanVisibleContent(input: string) {
  let text = input

  // Remove meta section titles that are useful for prompting but noisy in final UI.
  text = text.replace(
    /^\s*(?:#{1,6}\s*)?(?:问题归纳与理解|问题总结|详细回答|参考资源或链接|结束语或引导)\s*[:：]?\s*$/gim,
    '',
  )

  // Hide model self-explanation lines like "用户希望...".
  text = text.replace(/^\s*(?:用户希望|用户想了解|用户询问|该问题是关于)[^\n]*$/gim, '')

  // Strip list bullets that only contain meta section names.
  text = text.replace(
    /^\s*(?:[-*]|\d+\.)\s*(?:问题归纳与理解|问题总结|详细回答|参考资源或链接|结束语或引导)\s*[:：]?\s*$/gim,
    '',
  )

  return text.replace(/\n{3,}/g, '\n\n').trim()
}
</script>

<template>
  <div class="qa-page">
    <div class="header">
      <div class="title">学涯助手</div>
      <div class="sub">你的学习与求职陪练伙伴</div>
    </div>

    <el-card class="chat-card" shadow="never">
      <div class="chat-toolbar">
        <div class="toolbar-tip">直接提问就好：回车发送，Shift + Enter 换行</div>
        <div class="toolbar-actions">
          <el-button type="warning" size="small" @click="onStop" :disabled="!loading">停止生成</el-button>
          <el-button type="default" size="small" @click="onClear" :disabled="loading">清空对话</el-button>
        </div>
      </div>

      <div class="chat" ref="chatRef">
        <div v-for="(it, idx) in items" :key="idx" class="msg" :class="it.role">
          <div class="meta">
            <span class="role">{{ it.role === 'user' ? '我' : it.role === 'assistant' ? '学涯助手' : '系统消息' }}</span>
            <span class="time">{{ new Date(it.ts).toLocaleString() }}</span>
          </div>
          <pre v-if="it.role === 'user'" class="content plain">{{ it.content }}</pre>
          <div v-else class="content markdown" v-html="renderMarkdown(it.content)" />
        </div>
      </div>
    </el-card>

    <el-card class="composer-card" shadow="never">
      <el-input
        v-model="question"
        type="textarea"
        :autosize="{ minRows: 3, maxRows: 6 }"
        :maxlength="MAX_QUESTION_LEN"
        show-word-limit
        placeholder="例如：我想在两周内系统准备产品经理实习面试，应该怎么安排？"
        @keydown.enter.exact.prevent="onSend"
      />

      <div class="composer-actions">
        <div class="left-info">
          <div class="hint" v-if="errorText">{{ errorText }}</div>
          <div class="tip" v-else>多告诉我一些细节，我能给你更量身定制的建议哦 ~</div>
        </div>
        <div class="right-actions">
          <span class="counter">{{ questionLength }}/{{ MAX_QUESTION_LEN }}</span>
          <el-button type="primary" :loading="loading" :disabled="!canSend" @click="onSend">
            发送问题
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.qa-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header {
  margin-bottom: 4px;
}

.title {
  font-size: var(--ccp-title-size);
  font-weight: var(--ccp-title-weight);
  color: var(--ccp-text);
}

.sub {
  margin-top: 2px;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-sub-color);
}

.chat-card,
.composer-card {
  border-radius: var(--ccp-card-radius);
  border: 1px solid var(--ccp-card-border);
  background: var(--ccp-card-bg);
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.toolbar-tip {
  font-size: 12px;
  color: var(--ccp-text-muted);
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 280px;
  max-height: 58vh;
  overflow-y: auto;
  padding-top: 10px;
}

.msg {
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(248, 250, 252, 0.8);
}

.msg.user {
  border-color: rgba(99, 102, 241, 0.24);
  background: rgba(99, 102, 241, 0.07);
}

.msg.assistant {
  border-color: rgba(34, 197, 94, 0.24);
  background: rgba(34, 197, 94, 0.08);
}

.msg.system {
  border-style: dashed;
  background: rgba(148, 163, 184, 0.06);
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--ccp-text-muted);
}

.meta .role {
  font-weight: 700;
  color: var(--ccp-text-secondary);
}

.content {
  margin: 0;
  font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
  font-size: 14px;
  line-height: 1.6;
  color: var(--ccp-text);
}

.content.plain {
  white-space: pre-wrap;
  word-break: break-word;
}

.content.markdown {
  word-break: break-word;
}

.content.markdown :deep(h1),
.content.markdown :deep(h2),
.content.markdown :deep(h3),
.content.markdown :deep(h4) {
  margin: 0.7em 0 0.45em;
  line-height: 1.35;
}

.content.markdown :deep(p) {
  margin: 0.5em 0;
}

.content.markdown :deep(ul),
.content.markdown :deep(ol) {
  margin: 0.45em 0 0.75em;
  padding-left: 1.2em;
}

.content.markdown :deep(li + li) {
  margin-top: 0.2em;
}

.content.markdown :deep(blockquote) {
  margin: 0.6em 0;
  padding: 0.4em 0.8em;
  border-left: 3px solid rgba(99, 102, 241, 0.5);
  background: rgba(99, 102, 241, 0.08);
  border-radius: 6px;
}

.content.markdown :deep(code) {
  background: rgba(148, 163, 184, 0.18);
  border-radius: 4px;
  padding: 1px 4px;
}

.content.markdown :deep(pre) {
  background: rgba(15, 23, 42, 0.92);
  color: #f8fafc;
  border-radius: 8px;
  padding: 10px;
  overflow-x: auto;
}

.content.markdown :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
}

.content.markdown :deep(a) {
  color: #2563eb;
  text-decoration: underline;
}

.composer-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.left-info {
  min-height: 20px;
}

.hint {
  font-size: 12px;
  color: #ef4444;
}

.tip {
  font-size: 12px;
  color: var(--ccp-text-muted);
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.counter {
  font-size: 12px;
  color: var(--ccp-text-light);
}

@media (max-width: 768px) {
  .chat-toolbar,
  .composer-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .right-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>

