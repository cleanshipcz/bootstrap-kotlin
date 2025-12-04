const Colors = {
  primary: '#336699',
  secondary: '#86BBD8',
  dark: '#2F4858',
  accent: '#9EE493',
  background: '#DAF7DC',
} as const

type ColorKey = keyof typeof Colors

const hexToRgb = (hex: string) => {
  const normalized = hex.replace('#', '')
  const bigint = parseInt(normalized, 16)
  const r = (bigint >> 16) & 255
  const g = (bigint >> 8) & 255
  const b = bigint & 255
  return { r, g, b }
}

const rgbToHex = (r: number, g: number, b: number) =>
  `#${[r, g, b]
    .map((value) => {
      const hex = value.toString(16)
      return hex.length === 1 ? `0${hex}` : hex
    })
    .join('')}`

const lighten = (hex: string, amount: number) => {
  const { r, g, b } = hexToRgb(hex)
  const nextR = Math.round(r + (255 - r) * amount)
  const nextG = Math.round(g + (255 - g) * amount)
  const nextB = Math.round(b + (255 - b) * amount)
  return rgbToHex(nextR, nextG, nextB)
}

const rgbTuple = (hex: string) => {
  const { r, g, b } = hexToRgb(hex)
  return `${r} ${g} ${b}`
}

const setColorVar = (name: string, value: string) => {
  if (typeof document === 'undefined') {
    return
  }
  document.documentElement.style.setProperty(name, value)
}

export const applyColorPalette = () => {
  if (typeof document === 'undefined') {
    return
  }

  (Object.keys(Colors) as ColorKey[]).forEach((key) => {
    const hex = Colors[key]
    setColorVar(`--color-${key}`, hex)
    setColorVar(`--color-${key}-rgb`, rgbTuple(hex))
  })

  const surface = lighten(Colors.background, 0.35)
  const card = lighten(Colors.secondary, 0.2)
  const border = lighten(Colors.dark, 0.55)
  const muted = lighten(Colors.dark, 0.4)

  setColorVar('--color-surface', surface)
  setColorVar('--color-surface-rgb', rgbTuple(surface))
  setColorVar('--color-card', card)
  setColorVar('--color-card-rgb', rgbTuple(card))
  setColorVar('--color-border', border)
  setColorVar('--color-border-rgb', rgbTuple(border))
  setColorVar('--color-text', Colors.dark)
  setColorVar('--color-text-rgb', rgbTuple(Colors.dark))
  setColorVar('--color-text-muted', muted)
  setColorVar('--color-text-muted-rgb', rgbTuple(muted))
}

export { Colors }

