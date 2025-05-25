/**
 * 禁用开发者工具相关功能
 * 包括F12、右键菜单、常用开发者工具快捷键等
 */

// 禁用的快捷键说明
// F12 - 开发者工具
// Ctrl+Shift+I - 开发者工具
// Ctrl+Shift+J - 控制台
// Ctrl+Shift+C - 元素选择器
// Ctrl+U - 查看源码
// Ctrl+S - 保存页面

/**
 * 禁用键盘快捷键
 */
function disableKeyboardShortcuts() {
  document.addEventListener('keydown', (event) => {
    // 禁用F12
    if (event.key === 'F12') {
      event.preventDefault()
      event.stopPropagation()
      showWarning()
      return false
    }

    // 禁用Ctrl+Shift+I (开发者工具)
    if (event.ctrlKey && event.shiftKey && event.key === 'I') {
      event.preventDefault()
      event.stopPropagation()
      showWarning()
      return false
    }

    // 禁用Ctrl+Shift+J (控制台)
    if (event.ctrlKey && event.shiftKey && event.key === 'J') {
      event.preventDefault()
      event.stopPropagation()
      showWarning()
      return false
    }

    // 禁用Ctrl+Shift+C (元素选择器)
    if (event.ctrlKey && event.shiftKey && event.key === 'C') {
      event.preventDefault()
      event.stopPropagation()
      showWarning()
      return false
    }

    // 禁用Ctrl+U (查看源码)
    if (event.ctrlKey && event.key === 'U') {
      event.preventDefault()
      event.stopPropagation()
      showWarning()
      return false
    }

    // 禁用Ctrl+S (保存页面)
    if (event.ctrlKey && event.key === 's') {
      event.preventDefault()
      event.stopPropagation()
      return false
    }
  })
}

/**
 * 禁用右键菜单
 */
function disableContextMenu() {
  document.addEventListener('contextmenu', (event) => {
    event.preventDefault()
    event.stopPropagation()
    showWarning()
    return false
  })
}

/**
 * 禁用文本选择
 */
function disableTextSelection() {
  document.addEventListener('selectstart', (event) => {
    event.preventDefault()
    return false
  })

  document.addEventListener('dragstart', (event) => {
    event.preventDefault()
    return false
  })
}

/**
 * 检测开发者工具是否打开
 */
function detectDevTools() {
  let devtools = false
  const threshold = 160

  setInterval(() => {
    if (window.outerHeight - window.innerHeight > threshold ||
        window.outerWidth - window.innerWidth > threshold) {
      if (!devtools) {
        devtools = true
        handleDevToolsOpen()
      }
    } else {
      devtools = false
    }
  }, 500)
}

/**
 * 处理开发者工具被打开的情况
 */
function handleDevToolsOpen() {
  // 可以选择重定向到其他页面或显示警告
  showWarning()

  // 可选：关闭当前页面
  // window.close()

  // 可选：重定向到首页
  // window.location.href = '/'
}

/**
 * 显示警告信息
 */
function showWarning() {
  console.clear()
  console.log('%c⚠️ 警告', 'color: red; font-size: 20px; font-weight: bold;')
  console.log('%c Developer tools detected, please close them and continue using!', 'color: red; font-size: 14px;')
}

/**
 * 清空控制台
 */
function clearConsole() {
  setInterval(() => {
    console.clear()
  }, 1000)
}

/**
 * 初始化所有禁用功能
 */
export function initDisableDevTools() {
  // 只在生产环境启用
  if (import.meta.env.MODE === 'production') {
    disableKeyboardShortcuts()
    disableContextMenu()
    disableTextSelection()
    detectDevTools()
    clearConsole()

    console.log('%c🔒 TypeScript layer developer tool protection enabled', 'color: blue; font-size: 12px;')
  } else {
    console.log('%c🔧 Development environment: TypeScript layer developer tool protection disabled', 'color: orange; font-size: 12px;')
  }
}

/**
 * 仅禁用基础功能（不包括开发者工具检测）
 * 适用于需要保留部分开发功能的场景
 */
export function initBasicProtection() {
  disableContextMenu()
  disableTextSelection()

  // 只禁用部分快捷键
  document.addEventListener('keydown', (event) => {
    // 禁用Ctrl+U (查看源码)
    if (event.ctrlKey && event.key === 'U') {
      event.preventDefault()
      showWarning()
      return false
    }

    // 禁用Ctrl+S (保存页面)
    if (event.ctrlKey && event.key === 's') {
      event.preventDefault()
      return false
    }
  })
}
