const originalDlopen = process.dlopen

process.dlopen = function (module, filename, flags) {
  const file = String(filename || '')
  if (file.endsWith('.node')) {
    console.warn(`[BLOCK NATIVE] ${file}`)
    throw new Error(`Blocked native module: ${file}`)
  }
  return originalDlopen.apply(this, arguments)
}
