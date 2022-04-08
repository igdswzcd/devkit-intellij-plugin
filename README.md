# KunpengIntellIJPlugin

鲲鹏开发套件，包括代码迁移和性能分析等一系列软件工具，IntelliJ插件作为代码迁移工具和性能分析工具的客户端，配合安装在服务器上的服务端一起帮助开发者加速应用迁移和算力升级。
## 代码迁移工具
鲲鹏代码迁移工具是一款可以简化客户应用迁移到基于鲲鹏916/920的服务器过程的工具。本工具仅适用于开发和测试环境。仅支持x86 Linux软件迁移到鲲鹏Linux上的扫描、分析与迁移。

-软件迁移评估：对待迁移的x86软件进行扫描分析，给出可迁移性评估，同时提供鲲鹏平台上兼容的依赖文件下载链接。
-源码迁移：自动扫描并分析软件代码（包括C/C++/Fortran/汇编软件），评估迁移所需替换的依赖文件，并给出需修改建议。在识别x86汇编指令的同时，常用x86汇编指令被翻译成功能对等的鲲鹏汇编指令。修改建议可指导用户快速完成修改，建议中包含的源码甚至可一键替换，直接编译使用。
-软件包重构：分析x86平台上Linux软件包的构成及依赖性，将平台相关的依赖文件替换为鲲鹏平台兼容的版本，并重构成适用于鲲鹏平台的软件包。
-专项软件迁移：支持将部分专项软件源码一键自动化迁移修改、编译并构建成鲲鹏平台兼容的软件包，帮助用户快速迁移几类解决方案中常见的专项软件。
-增强功能：支持软件代码质量的静态检查功能，如在64位环境中运行的兼容性检查、结构体字节对齐检查、缓存行对齐检查和弱内存序检查等增强功能。
## 性能分析工具
鲲鹏性能分析是一个工具集，包含：系统性能分析、Java性能分析、系统诊断和调优助手。
### 系统性能分析
系统性能分析是针对基于鲲鹏916/920的服务器的性能分析和优化工具，能收集服务器的处理器硬件、操作系统、进程/线程、函数等各层次的性能数据，分析出系统性能指标，定位到系统瓶颈点及热点函数。
### Java性能分析
Java性能分析是针对TaiShan服务器上运行的Java程序进行性能分析和优化的工具，支持对本地或者远程服务器上的Java程序进行分析优化，能图形化显示Java程序的堆，线程，锁，垃圾回收等信息，收集热点函数，定位程序瓶颈点，帮助用户采取针对性优化。
### 系统诊断工具
系统诊断工具通过分析系统运行指标，识别异常点，例如：内存泄漏、内存越界、网络丢包等，并给出优化建议。工具还支持压测系统，如：网络IO，评估系统最大性能。
### 调优助手
调优助手工具通过系统化组织和分析性能指标、热点函数、系统配置等信息，形成系统资源消耗链条，引导用户根据优化路径分析性能瓶颈，并针对每条优化路径给出优化建议和操作指导，以此实现快速调优。

# 构建
构建性能分析工具客户端，首先调用./tuning_build_webview.sh构建webview部分，然后再调用./build.sh hypertuner
构建代码迁移工具客户端，首先调用./build_webview.sh构建webview部分，然后再调用：./build.sh portingadvisor