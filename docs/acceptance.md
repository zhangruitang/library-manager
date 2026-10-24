# 验收记录与复现步骤

本文档记录本项目在 2026-10-24 的验收结果，并给出可重复执行的手工步骤。验证对象是当前仓库中的 `src/`、`run.ps1` 和 `samples/`。所有实验都使用 `data/` 下的临时副本，不会改动示例文件；命令均在项目目录下用 Windows PowerShell 执行。

## 验证环境

- Windows，Windows PowerShell 5.1。
- Eclipse Temurin JDK 25.0.4；编译时使用 `javac --release 17`，验证 Java 17 编译目标，与应用要求的 JDK 17 或更高版本一致。
- 数据文件为 UTF-8（无 BOM）、Tab 分隔的文本，格式说明见 [README](../README.md)。

## 验收结果

| 检查项 | 结果 |
| --- | --- |
| `run.ps1 -Action build` 编译 | 通过，输出 `BUILD OK`，退出码 0 |
| V2 示例加载、显示全部、保存 | 通过：4 本图书，`B003` 已借出；保存后首行为 `LIBRARY<TAB>2`，每条记录 5 列 |
| V1 示例升级为 V2 | 通过：2 本图书，`B002` 已借出；保存后首行为 `LIBRARY<TAB>2`，每条记录 5 列，分类补为「未分类」 |
| 分类查询、带分类添加、修改分类 | 通过 |
| 分类与借阅状态互不覆盖，保存并重启后恢复 | 通过 |
| 错误文件头、错误列数、空分类、非法状态、重复编号拒绝加载 | 通过：退出码 1，文件内容不变 |
| 正常退出与输入流结束时保存 | 通过：退出码 0 |
| 保存失败 | 通过：退出码 2 |
| UTF-8 BOM 开头的文件 | 通过：判为「第1行：文件头不正确」，退出码 1 |

以上各项均按下文步骤实际执行并核对。

## 准备

先创建 `data/` 并复制新版示例。每次实验前重新复制，避免上一次操作影响下一次结果。

```powershell
New-Item -ItemType Directory -Force .\data | Out-Null
Copy-Item .\samples\books-v2.tsv .\data\acceptance-v2.tsv -Force
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\acceptance-v2.tsv
```

`-DataFile` 的相对路径按项目目录计算。菜单出现后，按表格逐项输入，每项输入后按回车；输入 `0` 保存并退出。

## 新版示例与分类功能

| 实验 | 依次输入 | 预期结果 |
| --- | --- | --- |
| 1. 按分类查询 | `12` → `编程` | 只显示 `B001`、`B002`，不显示文学和其他分类的图书 |
| 2. 新增带分类图书 | `10` → `B005` → `网络练习` → `示例作者戊` → `编程`，再执行实验 1 | 提示添加成功，查询结果中还应有 `B005` |
| 3. 修改分类 | `11` → `B001` → `文学`；随后分别用 `12` 查询「编程」和「文学」 | 编程结果不含 `B001`；文学结果含 `B001` 和 `B003` |
| 4. 分类和借阅状态互不覆盖 | `4` → `B002`；再 `11` → `B002` → `其他`；再 `3` → `B002`；最后 `5` → `B002` | 查询显示「其他」和「已借出」；归还后分类仍为「其他」 |
| 5. 保存并重启 | 先做实验 3、4，再输入 `7`（保存后继续使用）、`0`（保存并退出）；用原启动命令重新进入并输入 `2` | 提示保存成功；分类和借阅状态与退出前一致；文件首行为 `LIBRARY<TAB>2`，每条记录 5 列 |
| 6. 默认分类 | `1` → `B006` → `普通添加示例` → `示例作者己`；再 `3` → `B006` | 分类为「未分类」，初始状态为「可借阅」 |
| 7. 拒绝空分类 | `11` → `B002` → 直接回车；再 `3` → `B002` | 提示「分类不能为空」，该书原来的分类和借阅状态保持不变 |
| 8. 完整匹配与忽略大小写 | `11` → `B001` → `Programming`；用 `12` 查询 ` programming `，再查询 `Program` | 前一种查询能找到 `B001`，后一种查不到；输入两端空格被去除，分类查询是完整匹配，不做内部单词的包含匹配 |

## 旧格式升级为 V2

```powershell
New-Item -ItemType Directory -Force .\data | Out-Null
Copy-Item .\samples\books-v1.tsv .\data\acceptance-v1.tsv -Force
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\acceptance-v1.tsv
```

1. 输入 `2`：两本书的分类都显示「未分类」，`B002` 显示「已借出」。
2. 输入 `0`：提示保存成功并退出。
3. 用编辑器查看 `data/acceptance-v1.tsv`：首行为 `LIBRARY`、Tab、`2`；两条图书记录各 5 列，最后一列为「未分类」。原始文件 `samples/books-v1.tsv` 保持不变。
4. 再次执行原启动命令并输入 `2`：书名、作者、分类、借阅状态保持一致；再次保存后文件内容不变。

文件名不决定数据版本，首行决定版本；V1 加载后只能保存为 V2，本项目没有导出回 V1 的操作。

## 错误文件保护

每次从示例重新复制副本，用编辑器改动后运行。可以在运行前后各计算一次哈希，确认文件没有被改写。

```powershell
New-Item -ItemType Directory -Force .\data | Out-Null
Copy-Item .\samples\books-v2.tsv .\data\bad-books.tsv -Force
Get-FileHash .\data\bad-books.tsv -Algorithm SHA256
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\bad-books.tsv
Get-FileHash .\data\bad-books.tsv -Algorithm SHA256
```

| 对副本的改动 | 预期提示 |
| --- | --- |
| 把第 1 行版本 `2` 改为 `3` | 第1行：文件头不正确 |
| 删除第 2 行末尾的分类和它前面的 Tab，使其只剩 4 列 | 第2行：图书记录应有5列 |
| 清空第 2 行分类，保留它前面的 Tab | 第2行：分类不能为空 |
| 把第 2 行状态改为 `LOST` | 第2行：借阅状态不正确 |
| 把第 3 行编号改成与第 2 行相同的 `B001` | 第3行：重复编号 B001 |

以上情况都会在进入菜单前终止启动，退出码为 1；再次打开该副本时，故意写入的错误内容仍在，没有被空书库覆盖。若把文件保存成「UTF-8 带 BOM」，首行会包含 BOM，同样被判为「第1行：文件头不正确」，因此编辑器保存时应选择「UTF-8」（无 BOM）。程序自身保存的文件不带 BOM。

## 退出码

| 退出码 | 含义 | 复现方式 |
| --- | --- | --- |
| 0 | 正常完成并保存 | 输入 `0` 并保存成功；或输入流结束时保存成功 |
| 1 | 启动时读取书单失败 | 使用上一节的错误文件；不进入菜单，文件不会被改写 |
| 2 | 输入结束且保存失败 | 见下方步骤 |

保存失败的复现方法：把书单路径指向一个普通文件的子路径，程序仍能从空书库启动，但无法在该位置创建目录和文件。

```powershell
New-Item -ItemType Directory -Force .\data | Out-Null
Set-Content -LiteralPath .\data\blocked -Value 'not a directory' -Encoding ascii
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\blocked\books.tsv
```

菜单出现后结束输入（在控制台按 Ctrl+Z 再按回车；效果等同于输入流结束），程序尝试保存，提示「保存失败：…」并以退出码 2 结束。如果先输入 `0`，保存失败后会留在菜单中，便于修复路径后重试；此时不会立即以 2 退出，要等到再次结束输入且保存失败时退出码才为 2。

实验结束后可以删除这些副本：

```powershell
Remove-Item .\data\acceptance-v2.tsv, .\data\acceptance-v1.tsv, .\data\bad-books.tsv -Force -ErrorAction SilentlyContinue
Remove-Item .\data\blocked -Force -ErrorAction SilentlyContinue
```

`data/` 已被 `.gitignore` 忽略，这些副本不会进入版本库。

## 自动化验证

仓库包含 GitHub Actions 工作流 [.github/workflows/ci.yml](../.github/workflows/ci.yml)。工作流在推送到 `main` 或 `master`、提交拉取请求、手动触发时运行，权限为只读（`contents: read`）。矩阵为 `ubuntu-latest` 和 `windows-latest` 两个平台、JDK 17 和 21 两个版本（Temurin），`fail-fast: false`。

- Windows：先执行 `powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Action build`，验证启动脚本本身；再编译到 `out/` 供冒烟测试使用。
- Linux：按 README 中的等价命令编译到 `out/`。
- 两个平台都执行冒烟测试：把 `samples` 中的书单复制到 `data/`，以菜单 `2`、`0` 的输入运行；断言退出码为 0、标准输出含 `B001`、保存后首行为 `LIBRARY<TAB>2`、每条记录 5 列；对 V1 示例断言升级后首行为 V2、每条记录 5 列、分类为「未分类」；再构造错误文件头，断言退出码为 1 且文件哈希不变。

与本文手工步骤的关系：CI 覆盖 JDK 17/21 和 Windows/Linux 下的编译与固定输入冒烟测试；分类查询、添加和修改、借还状态、菜单 `7` 与重启、保存失败、BOM 边界等交互场景需要按本文手工执行，未写入 CI。两者使用同一份 `samples`；Windows 上的 CI 还会调用 `run.ps1` 编译。手工步骤可用于核对 CI 之外的交互行为。本文记录的手工步骤在 Windows PowerShell 和 JDK 25（`--release 17`）上执行通过。

## 运行边界

- 本文命令针对 Windows PowerShell；`run.ps1` 是 Windows 的快捷启动脚本，README 也给出了不依赖脚本的 `javac` 和 `java` 命令。
- 验证使用 JDK 25.0.4 并指定 `--release 17`；CI 另外在 JDK 17 和 21 上编译与冒烟。未在 macOS 或 Linux 上验证 `run.ps1` 和交互输入。
- 交互实验依赖终端输入；CI 使用固定输入，不覆盖真实的菜单交互。
- 数据文件必须是 UTF-8（无 BOM）、Tab 分隔；BOM 开头的文件会被拒绝。
- 本项目是单用户控制台应用，不支持多个进程同时修改同一份书单，分类为自由文本；未覆盖文件系统故障下的原子替换等异常场景。
