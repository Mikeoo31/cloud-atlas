import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 * @param size
 */
export const formatSize = (size?: number) => {
  if (!size) return '未知'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

/**
 * 下载图片
 * @param url 图片下载地址
 * @param fileName 要保存为的文件名
 */
export function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  saveAs(url, fileName)
}

/**
 * 将颜色值转换为标准 #RRGGBB 格式
 * @param input
 */
export function toHexColor(input: string) {
  // 去掉 0x 前缀
  const colorValue = input.startsWith('0x') ? input.slice(2) : input

  // 将剩余部分解析为十六进制数，再转成 6 位十六进制字符串
  const hexColor = parseInt(colorValue, 16).toString(16).padStart(6, '0')

  // 返回标准 #RRGGBB 格式
  return `#${hexColor}`
}

const colors = [
  'magenta',
  'red',
  'volcano',
  'orange',
  'gold',
  'lime',
  'cyan',
  'blue',
  'geekblue',
  'purple',
];

/**
 * 生成随机颜色
 */
export const generateRandomColor = (): string => {
  return colors[Math.floor(Math.random() * colors.length)];
};

/**
 * 初始化标签颜色映射
 * @param tags 标签数组
 * @param existingColors 已存在的颜色映射
 * @returns 更新后的颜色映射
 */
export const initializeTagColors = (
  tags: string[],
  existingColors: Record<string, string> = {}
): Record<string, string> => {
  const tagColors = { ...existingColors };

  tags.forEach((tag) => {
    if (!tagColors[tag]) {
      tagColors[tag] = generateRandomColor();
    }
  });

  return tagColors;
};

