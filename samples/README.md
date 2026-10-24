# 示例书单

本目录提供两份虚构示例书单，用于首次试运行和格式参考。文件是 UTF-8（无 BOM）文本，列与列之间使用真正的 Tab 字符。完整的验收步骤见 [验收记录与复现步骤](../docs/acceptance.md)。

| 文件 | 内容 | 可以用来观察什么 |
| --- | --- | --- |
| `books-v1.tsv` | 旧格式（首行 `LIBRARY<TAB>1`），2 本图书，无分类列；`B002` 已借出 | 加载时分类补为「未分类」；保存后升级为 V2，借阅状态保留 |
| `books-v2.tsv` | 当前格式（首行 `LIBRARY<TAB>2`），4 本图书，分类为「编程」「文学」「其他」；`B003` 已借出 | 分类查询、修改分类、借还、保存并重启 |

推荐先复制到 `data/` 再运行复制出的书单。`data/` 已被 `.gitignore` 忽略，不会进入版本库。例如在项目目录执行：

```powershell
New-Item -ItemType Directory -Force .\data | Out-Null
Copy-Item .\samples\books-v2.tsv .\data\demo-v2.tsv
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\demo-v2.tsv
```

程序会把操作结果写回 `-DataFile` 指定的文件；直接对 `samples` 里的文件运行，退出保存时会改动示例本身，因此不建议这样做。

`books-v1.tsv` 中的「v1」只是文件名提示。迁移后，复制品即使仍叫原来的文件名，文件首行也会是 `LIBRARY<TAB>2`；数据版本由首行决定，不由文件名决定。示例文件以 UTF-8（无 BOM）保存；如果编辑器另存为带 BOM，程序会判为「第1行：文件头不正确」，详见验收记录。

项目整体说明见 [README](../README.md)。
