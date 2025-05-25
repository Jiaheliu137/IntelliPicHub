/**
 * 禁用开发者工具的工具函数
 * 包含多种方法来阻止用户打开开发者工具
 */

// 禁用右键菜单
function disableContextMenu() {
  document.addEventListener('contextmenu', (e) => {
    e.preventDefault()
    return false
  })
}

// 禁用F12和其他开发者工具快捷键
function disableKeyboardShortcuts() {
  document.addEventListener('keydown', (e) => {
    // 禁用F12
    if (e.key === 'F12') {
      e.preventDefault()
      return false
    }

    // 禁用Ctrl+Shift+I (开发者工具)
    if (e.ctrlKey && e.shiftKey && e.key === 'I') {
      e.preventDefault()
      return false
    }

    // 禁用Ctrl+Shift+J (控制台)
    if (e.ctrlKey && e.shiftKey && e.key === 'J') {
      e.preventDefault()
      return false
    }

    // 禁用Ctrl+U (查看源代码)
    if (e.ctrlKey && e.key === 'u') {
      e.preventDefault()
      return false
    }

    // 禁用Ctrl+Shift+C (选择元素)
    if (e.ctrlKey && e.shiftKey && e.key === 'C') {
      e.preventDefault()
      return false
    }

    // 禁用Ctrl+S (保存页面)
    if (e.ctrlKey && e.key === 's') {
      e.preventDefault()
      return false
    }
  })
}

// 检测开发者工具是否打开
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

// 当检测到开发者工具打开时的处理
function handleDevToolsOpen() {
  // 可以选择以下任一种处理方式：

  // 方式1: 关闭当前页面
  // window.close()

  // 方式2: 跳转到其他页面
  // window.location.href = 'about:blank'

  // 方式3: 显示警告信息
  alert('检测到开发者工具，请关闭后继续使用！')

  // 方式4: 刷新页面
  // window.location.reload()
}

// 禁用选择文本
function disableTextSelection() {
  document.addEventListener('selectstart', (e) => {
    e.preventDefault()
    return false
  })

  document.addEventListener('dragstart', (e) => {
    e.preventDefault()
    return false
  })
}

// 禁用打印
function disablePrint() {
  window.addEventListener('beforeprint', (e) => {
    e.preventDefault()
    return false
  })

  window.addEventListener('afterprint', (e) => {
    e.preventDefault()
    return false
  })
}

// 覆盖console对象（仅在生产环境）
function disableConsole() {
  if (import.meta.env.PROD) {
    const noop = () => {}
    const methods = ['log', 'debug', 'info', 'warn', 'error', 'assert', 'dir', 'dirxml', 'group', 'groupEnd', 'time', 'timeEnd', 'count', 'trace', 'profile', 'profileEnd']

    methods.forEach(method => {
      (console as any)[method] = noop
    })
  }
}

// 主函数：启用所有保护措施
export function initDevToolsProtection() {
  // 只在生产环境启用
  if (import.meta.env.PROD) {
    disableContextMenu()
    disableKeyboardShortcuts()
    detectDevTools()
    disableTextSelection()
    disablePrint()
    disableConsole()

    console.log('开发者工具保护已启用')
  } else {
    console.log('开发环境，跳过开发者工具保护')
  }
}

// 轻量级版本：只禁用基本功能
export function initBasicProtection() {
  disableContextMenu()
  disableKeyboardShortcuts()

  console.log('基本保护已启用')
}
