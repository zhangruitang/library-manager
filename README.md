# Java 图书借阅管理器

[![CI](https://github.com/zhangruitang/library-manager/actions/workflows/ci.yml/badge.svg)](https://github.com/zhangruitang/library-manager/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-orange)

一个只依赖 JDK 的控制台图书借阅管理器：添加、查询、修改、删除图书，借阅与归还，按分类管理，数据保存在可直接阅读的 TSV 文本文件里。项目源自 Java 学习过程，已整理成可以独立编译、运行和发布的小应用。

> A dependency-free Java 17+ console application for a small book library: CRUD, borrow/return, categories, and a human-readable TSV data file. Built as a learning project; no database and no third-party libraries.

## 目录

- [功能](#功能)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [运行效果](#运行效果)
- [菜单与操作规则](#菜单与操作规则)
- [数据文件格式](#数据文件格式)
- [示例书单](#示例书单)
- [目录结构](#目录结构)
- [验证](#验证)
- [常见问题](#常见问题)
- [已知限制](#已知限制)
- [许可证](#许可证)

## 功能

- 图书增删改查、借阅与归还、书名关键词查询、分类添加/修改/查询。
- 字段校验：编号、书名、作者、分类不能为空，也不能包含 Tab 或换行符；编号不能重复；已借出的图书不能重复借出或删除。
- 数据可读：使用 UTF-8 的 TSV 文本文件，可以直接用编辑器查看，而不是二进制数据库。
- 版本兼容：可读取 V1（4 列）和 V2（5 列，带分类）两种文件，V1 会在保存时自动升级为 V2。
- 保存可靠：先写同目录临时文件再替换目标文件，尽量降低写入中断导致数据损坏的风险。
- 零第三方依赖：只需要 JDK 17+，不需要数据库、构建工具或网络。

## 环境要求

- JDK 17 或更高版本，需要能在终端执行 `java -version` 和 `javac -version`。可以从 [Adoptium](https://adoptium.net/) 或系统包管理器安装。
- Windows 上运行 `run.ps1` 需要 Windows PowerShell 5.1 或 PowerShell 7+。
- macOS 和 Linux 只需要 JDK；下方提供了不依赖脚本的编译运行命令。
- 项目没有使用 Maven、Gradle 或第三方 Java 依赖，所有源码都在 `src` 中。

本地开发验证使用 Windows 11 + Eclipse Temurin JDK 25.0.4，编译参数固定为 `--release 17`；CI 在 JDK 17 和 21 上分别编译并运行冒烟测试。

## 快速开始

先获取代码（二选一）：

```powershell
git clone https://github.com/zhangruitang/library-manager.git
cd library-manager
```

也可以直接在 GitHub 页面选择 **Code → Download ZIP**，解压后进入 `library-manager` 目录。

### Windows

在项目目录的 PowerShell 中执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

脚本会编译 `src` 里的 Java 文件，然后启动 `library.Main`。出现菜单后，输入数字并按回车即可操作。

`-ExecutionPolicy Bypass` 只影响这次启动的 PowerShell 进程，不会永久修改系统的执行策略。

首次运行时，如果 `data/books.tsv` 不存在，程序会从空书库开始；第一次保存时自动创建目录和文件。`data` 和编译输出都不会被 Git 跟踪。

### macOS / Linux

在项目目录执行：

```bash
mkdir -p out
find src -name '*.java' -print > sources.txt
javac --release 17 -encoding UTF-8 -d out @sources.txt
rm sources.txt
java -Dfile.encoding=UTF-8 -cp out library.Main data/books.tsv
```

`data/books.tsv` 不存在时会从空书库开始；也可以省略该参数，程序默认同样读取 `data/books.tsv`。

### 在 VS Code 中运行

用 VS Code 打开本 `library-manager` 文件夹，并安装支持 Java 运行与调试的扩展，例如 Extension Pack for Java。打开 `src/library/Main.java`，点击 `main` 方法上方的 **Run**，或按 F5 选择“运行图书管理器”。附带的 `.vscode` 配置已指定默认书单路径。

运行按钮和 `run.ps1` 是两种独立的启动方式，使用运行按钮时不需要执行脚本。

### 只编译，或指定另一份书单

```powershell
# 只检查编译，不进入菜单。
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Action build

# 使用另一份数据文件；相对路径按本项目目录计算。
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\my-books.tsv
```

编译输出放在临时的 `.build` 目录中，脚本退出时会自动清理，包括编译失败或程序异常退出的情况。

### 不使用脚本运行（Windows）

在项目目录的 PowerShell 中依次执行：

```powershell
chcp 65001 | Out-Null   # 让传统 Windows 控制台按 UTF-8 处理中文输入输出
New-Item -ItemType Directory -Force .\out | Out-Null
$javaFiles = @(Get-ChildItem .\src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
javac -encoding UTF-8 --release 17 -d .\out @javaFiles
java '-Dfile.encoding=UTF-8' -cp .\out library.Main
```

在 Windows PowerShell 5.1 中，`-D...` 形式的 JVM 参数必须像上面这样用引号包住；不加引号会被 PowerShell 拆成两个参数，导致“找不到或无法加载主类”的错误。

编译失败时，应先处理报错，再执行最后一行。Java 源码本身也可以在任何支持 JDK 17 的环境中编译运行。

## 运行效果

下面是使用 `samples/books-v2.tsv` 时的真实输出（省略了重复的菜单提示）：

```text
请输入菜单：1添加 | 2全部 | 3编号查询 | 4借出 | 5归还 | 6删除 | 7保存继续 | 8修改 | 9书名查询 | 10分类添加 | 11修改分类 | 12分类查询 | 0退出
2
B001 | Java 入门练习 | 示例作者甲 | 编程 | 可借阅
B002 | 算法练习笔记 | 示例作者乙 | 编程 | 可借阅
B003 | 校园故事集 | 示例作者丙 | 文学 | 已借出
B004 | 学习方法手册 | 示例作者丁 | 其他 | 可借阅
```

## 菜单与操作规则

| 输入 | 功能 | 接下来依次输入 |
| --- | --- | --- |
| `1` | 添加图书，分类默认为“未分类” | 编号、书名、作者 |
| `2` | 查看全部图书 | 无 |
| `3` | 按编号查询 | 编号 |
| `4` | 借出图书 | 编号 |
| `5` | 归还图书 | 编号 |
| `6` | 删除图书 | 编号；已借出的图书不能删除 |
| `7` | 保存后继续使用 | 无 |
| `8` | 修改书名和作者 | 编号、新书名、新作者 |
| `9` | 按书名关键词查询 | 关键词 |
| `10` | 添加带分类的图书 | 编号、书名、作者、分类 |
| `11` | 修改分类 | 编号、新分类 |
| `12` | 按分类查询 | 分类 |
| `0` | 保存并退出 | 无 |

匹配规则：

- 编号匹配区分大小写，查找时去除输入两端的空白。
- 书名查询忽略大小写，是包含匹配。
- 分类查询去除输入两端空白后，按完整分类名忽略大小写匹配；例如分类 `Programming` 可以用 `programming` 查到，分类“编程”不会匹配“编”。

输入约束：

- 编号、书名、作者和分类都不能为空，也不能包含制表符或换行符。
- 保存时去除字段两端空白，保留内部普通空格。
- 编号不能重复；已借出的图书不能重复借出；尚未借出的图书不能执行归还。
- 输入未知菜单项时会提示“没有这个菜单选项”，然后回到菜单。

## 数据文件格式

默认书单为项目内的 `data/books.tsv`，使用 UTF-8 编码，列与列之间用真正的 Tab 字符分隔。它是普通文本文件，可以用 VS Code 查看和编辑；分隔符不能写成 `\t` 两个字符。

| 版本 | 文件首行 | 后续各行的列 |
| --- | --- | --- |
| V1 | `LIBRARY<TAB>1` | 编号、书名、作者、状态，共 4 列 |
| V2 | `LIBRARY<TAB>2` | 编号、书名、作者、状态、分类，共 5 列 |

上表的 `<TAB>` 只是说明分隔符，实际文件请参考 `samples`。状态只接受 `AVAILABLE`（可借阅）和 `BORROWED`（已借出）。

编码和换行有一些容易踩的细节：

- 文件必须是 UTF-8，并且**不要带 BOM**。BOM 会让程序把首行识别为错误文件头，提示“第1行：文件头不正确”。
- 换行符用 LF 或 CRLF 都可以读取；保存时使用当前操作系统的换行符（Windows 上为 CRLF，macOS/Linux 上为 LF）。
- 空分类、缺列、重复编号、非法状态都会让程序提示行号并终止启动，不会带着错误数据继续运行。
- 加载失败不会把书库当作空库保存；原文件保持原样。

版本迁移：

- 程序可以读取 V1 与 V2；V1 的分类会自动补为“未分类”。
- 每次保存统一写为 V2，包括从 V1 加载后未做修改便直接保存的情况。
- 升级前建议先复制一份旧书单留作备份；程序没有导出回 V1 的功能。
- 文件名不决定数据版本，首行才决定版本。

保存与退出：

- 添加、修改、借还和删除先改变内存中的书库。菜单 `7` 保存并继续，菜单 `0` 保存并退出。
- 输入流结束时程序也会尝试保存已经完成的操作。
- 菜单 `0` 保存失败时会留在菜单中，修复路径问题后可以重试，不会直接退出。
- 直接关闭终端或强制结束程序，可能丢失上次保存之后的修改。

退出码：

| 退出码 | 含义 |
| --- | --- |
| `0` | 正常完成，包括输入结束且保存成功 |
| `1` | 启动时加载书单失败 |
| `2` | 输入结束且保存失败 |

## 示例书单

`samples` 中提供两份虚构数据，用于演示格式和试运行：

- `books-v1.tsv`：2 本旧格式图书，无分类列，`B002` 已借出。
- `books-v2.tsv`：4 本新版图书，分类为“编程”“文学”“其他”，`B003` 已借出。

建议先复制到 `data` 再运行，这样可以一直保留原始样本：

```powershell
New-Item -ItemType Directory -Force .\data | Out-Null
Copy-Item .\samples\books-v2.tsv .\data\demo-v2.tsv
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -DataFile .\data\demo-v2.tsv
```

进入菜单后输入 `2` 查看全部图书。`B003` 初始为“已借出”，可以用 `5` 归还；输入 `12` 再输入“编程”可以查看两本编程类图书。更多演示步骤和 V1 升级验证见 [samples/README.md](samples/README.md)。

不要直接用 `-DataFile .\samples\books-v2.tsv` 运行：退出保存会改写示例文件本身。

## 目录结构

```text
library-manager/
├─ src/library/
│  ├─ Main.java                  选择路径、加载书单、组装并启动程序
│  ├─ model/Book.java            一本书的数据、字段校验与借还状态
│  ├─ service/LibraryService.java 书库的增删改查和借还业务
│  ├─ storage/                  文件读写接口与 TSV 实现
│  └─ ui/ConsoleUI.java          菜单、用户输入和打印
├─ samples/                     V1、V2 示例书单及说明
├─ docs/acceptance.md           验收记录和可重复的操作步骤
├─ .github/workflows/ci.yml     GitHub Actions：编译与冒烟测试
├─ .vscode/                     VS Code 的 Java 运行配置
├─ .editorconfig                编辑器编码与缩进约定
├─ .gitattributes               Git 行尾与文本文件规则
├─ .gitignore                   Git 忽略规则
├─ LICENSE                      MIT 许可证
├─ run.ps1                      Windows 下的独立编译和启动脚本
└─ README.md                    本说明
```

首次保存后会产生 `data/`；手工编译可能产生 `bin/` 或 `out/`；`run.ps1` 会临时使用 `.build/`。这些目录都已加入 `.gitignore`。`.gitattributes` 统一了文本文件的行尾约定（LF），在提交和检出时生效，让不同操作系统的克隆结果保持一致。

## 验证

GitHub Actions 会在推送到 `main`/`master` 分支、创建拉取请求或手动触发时，在 Windows 与 Linux、JDK 17 与 21 组合上编译源码并运行非交互冒烟测试，覆盖示例书单加载、保存、V1→V2 升级和错误文件保护。工作流见 [.github/workflows/ci.yml](.github/workflows/ci.yml)。

本地可以只做编译检查：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Action build
```

成功时输出 `BUILD OK`，退出码为 `0`。

手工验收步骤、错误文件用例和复现命令见 [docs/acceptance.md](docs/acceptance.md)。项目没有引入 JUnit 或其他测试框架，这是为了保持“只依赖 JDK”的定位；主流程由 CI 冒烟测试覆盖，菜单交互和异常输入通过验收文档中的手工步骤验证。

## 常见问题

**提示找不到 `javac` 或 `java`。** 还没有安装 JDK，或 JDK 的 `bin` 目录不在 `PATH` 中。安装 JDK 17+ 后重新打开终端，用 `java -version` 和 `javac -version` 确认。

**Windows 上中文显示或输入乱码。** 推荐用 `run.ps1` 启动，它会设置 UTF-8 控制台编码。如果直接执行 `java`，请先在终端运行 `chcp 65001`，并确保数据文件是 UTF-8。

**启动提示“第1行：文件头不正确”。** 数据文件第 1 行应该是 `LIBRARY` + Tab + `1`（V1）或 `2`（V2）。常见原因是文件被编辑器改过、用了别的分隔符、或者保存成了带 BOM 的 UTF-8。可以用 `samples` 中的文件对照。

**保存失败，后面只跟着一个路径。** 通常是目标目录不可写，或目标路径的父级其实是一个文件。改用手上有写权限的目录，例如 `-DataFile .\data\books.tsv`；也可以用 `7` 重试保存。

**运行后没看到 `.build` 目录。** 这是正常的：脚本会在退出时清理编译输出；只有强制结束进程时才可能残留，手动删除即可。

**为什么 `data/` 没有被 Git 跟踪？** 书单属于本地数据，`.gitignore` 已忽略 `data/`；需要分享的示例放在 `samples/`。

**可以同时开两个程序改同一份书单吗？** 不建议。项目面向单人使用，没有文件锁或多进程同步，后保存的会覆盖先保存的。

## 已知限制

这是一个面向单人学习的控制台应用：

- 使用文本文件保存，不含图形界面、账号体系或数据库。
- 不支持多个进程同时修改同一份书单，建议一次只启动一个程序操作同一份数据。
- 分类使用自由文本，没有分类表、借阅人、到期日期或借阅历史管理。
- 没有自动化单元测试；CI 以编译和冒烟测试为主，交互细节依赖手工验收。
- 未针对超大书单做性能优化；实际学习和小型使用场景足够。

## 许可证

本项目基于 [MIT License](LICENSE) 发布，Copyright (c) 2026 zhangruitang。
