/**
 * 高级开发者工具保护
 * 包含更强的保护措施和代码混淆
 */

// 使用混淆的变量名和函数名
const _0x1a2b = {
  _0x3c4d: 'F12',
  _0x5e6f: 'I',
  _0x7g8h: 'J',
  _0x9i0j: 'u',
  _0x1k2l: 'C',
  _0x3m4n: 's'
}

// 创建多层保护
class DevToolsProtector {
  private _isProtected = false
  private _checkInterval: number | null = null
  private _debuggerTrap: number | null = null

  constructor() {
    this.init()
  }

  private init() {
    if (this._isProtected) return

    this._isProtected = true
    this.setupKeyboardProtection()
    this.setupContextMenuProtection()
    this.setupDevToolsDetection()
    this.setupDebuggerTrap()
    this.setupConsoleProtection()
    this.setupSourceProtection()
  }

  // 键盘保护
  private setupKeyboardProtection() {
    const handler = (e: KeyboardEvent) => {
      const key = e.key
      const ctrl = e.ctrlKey
      const shift = e.shiftKey

      // 使用混淆的键值检查
      if (key === _0x1a2b._0x3c4d ||
          (ctrl && shift && key === _0x1a2b._0x5e6f) ||
          (ctrl && shift && key === _0x1a2b._0x7g8h) ||
          (ctrl && key === _0x1a2b._0x9i0j) ||
          (ctrl && shift && key === _0x1a2b._0x1k2l) ||
          (ctrl && key === _0x1a2b._0x3m4n)) {
        e.preventDefault()
        e.stopPropagation()
        this.triggerProtection()
        return false
      }
    }

    document.addEventListener('keydown', handler, true)
    window.addEventListener('keydown', handler, true)
  }

  // 右键菜单保护
  private setupContextMenuProtection() {
    const handler = (e: Event) => {
      e.preventDefault()
      e.stopPropagation()
      this.triggerProtection()
      return false
    }

    document.addEventListener('contextmenu', handler, true)
    window.addEventListener('contextmenu', handler, true)
  }

  // 开发者工具检测
  private setupDevToolsDetection() {
    let devtools = false
    const threshold = 160

    const check = () => {
      const heightDiff = window.outerHeight - window.innerHeight
      const widthDiff = window.outerWidth - window.innerWidth

      if (heightDiff > threshold || widthDiff > threshold) {
        if (!devtools) {
          devtools = true
          this.handleDevToolsDetected()
        }
      } else {
        devtools = false
      }
    }

    this._checkInterval = window.setInterval(check, 300)

    // 额外的检测方法
    this.setupAdvancedDetection()
  }

  // 高级检测方法
  private setupAdvancedDetection() {
    // 检测console对象是否被修改
    const originalConsole = window.console
    let consoleOpened = false

    Object.defineProperty(window, 'console', {
      get() {
        if (!consoleOpened) {
          consoleOpened = true
          setTimeout(() => {
            this.handleDevToolsDetected()
          }, 100)
        }
        return originalConsole
      },
      set() {
        // 阻止修改console
      }
    })

    // 检测调试器
    const checkDebugger = () => {
      const start = performance.now()
      debugger
      const end = performance.now()

      if (end - start > 100) {
        this.handleDevToolsDetected()
      }
    }

    this._debuggerTrap = window.setInterval(checkDebugger, 1000)
  }

  // debugger陷阱
  private setupDebuggerTrap() {
    const trap = () => {
      try {
        const start = Date.now()
        debugger
        const end = Date.now()

        if (end - start > 100) {
          this.handleDevToolsDetected()
        }
      } catch (e) {
        // 忽略错误
      }

      setTimeout(trap, 2000)
    }

    setTimeout(trap, 1000)
  }

  // 控制台保护
  private setupConsoleProtection() {
    if (import.meta.env.PROD) {
      const noop = () => {}
      const methods = ['log', 'debug', 'info', 'warn', 'error', 'assert', 'dir', 'dirxml', 'group', 'groupEnd', 'time', 'timeEnd', 'count', 'trace', 'profile', 'profileEnd']

      methods.forEach(method => {
        try {
          (console as any)[method] = noop
        } catch (e) {
          // 忽略错误
        }
      })
    }
  }

  // 源码保护
  private setupSourceProtection() {
    // 禁用选择文本
    const disableSelection = (e: Event) => {
      e.preventDefault()
      return false
    }

    document.addEventListener('selectstart', disableSelection, true)
    document.addEventListener('dragstart', disableSelection, true)

    // 禁用打印
    window.addEventListener('beforeprint', (e) => {
      e.preventDefault()
      this.triggerProtection()
    })

    // 禁用保存页面
    document.addEventListener('keydown', (e) => {
      if (e.ctrlKey && e.key === 's') {
        e.preventDefault()
        this.triggerProtection()
      }
    })
  }

  // 触发保护措施
  private triggerProtection() {
    // 可以自定义保护行为
    console.warn('检测到不当操作')
  }

  // 处理开发者工具被检测到的情况
  private handleDevToolsDetected() {
    // 方式1: 显示警告
    alert('检测到开发者工具，请关闭后继续使用！')

    // 方式2: 跳转到空白页
    // window.location.href = 'about:blank'

    // 方式3: 刷新页面
    // window.location.reload()

    // 方式4: 关闭页面
    // window.close()
  }

  // 销毁保护
  public destroy() {
    if (this._checkInterval) {
      clearInterval(this._checkInterval)
      this._checkInterval = null
    }

    if (this._debuggerTrap) {
      clearInterval(this._debuggerTrap)
      this._debuggerTrap = null
    }

    this._isProtected = false
  }
}

// 导出初始化函数
export function initAdvancedProtection() {
  // 只在生产环境启用完整保护
  if (import.meta.env.PROD) {
    new DevToolsProtector()
    console.log('高级保护已启用')
  } else {
    console.log('开发环境，跳过高级保护')
  }
}

// 导出轻量级保护（开发环境也可用）
export function initLightProtection() {
  // 基本的键盘保护
  document.addEventListener('keydown', (e) => {
    if (e.key === 'F12' ||
        (e.ctrlKey && e.shiftKey && e.key === 'I') ||
        (e.ctrlKey && e.shiftKey && e.key === 'J')) {
      e.preventDefault()
      return false
    }
  })

  // 基本的右键保护
  document.addEventListener('contextmenu', (e) => {
    e.preventDefault()
    return false
  })

  console.log('轻量级保护已启用')
}
