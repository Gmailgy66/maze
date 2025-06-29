东北⼤学计算机学院
算法设计与分析课程设计任务书
2024-2025春季学期
算法驱动的迷宫探险游戏开发
⼀. 设计⽬标
1. 开发⼀款由经典算法设计策略全栈驱动的迷宫探险游戏，AI玩家
（算法控制）需从起点出发，穿越随机⽣成的迷宫，收集资源、
避开陷阱、破解机关，击败守卫BOSS，最终抵达终点。
能够通过分治、动态规划、贪⼼、回溯、分⽀限界算法解决迷宫
⽣成、资源分配、路径规划、解谜与战⽃等关键问题，从⽽提升
算法设计能⼒和⼯程实践能⼒。
2.
⼆. 任务分解
1. 采⽤分治法⽣成迷宫
任务：使⽤分治法⽣成迷宫，⽆孤⽴区域，存在唯⼀通路。
输⼊：迷宫尺⼨n × n。
输出：迷宫矩阵（可存储为JSON或CSV），包括起点Start（S）、
终点Exit（E）、墙壁（#）、通路（空格）、资源（例如⾦币G）、
陷阱Trap（T）、机关Locker（L）、BOSS（B）。例如：
S # # # # # # # # #
#   G # T     #   #
# # # # # #   # # #
#   L #       # T #
# # # # # # # #   #
#   B # # #   G   #
# # # #   # # # # #
# T   #   L #     #
# # # # # # # #   #
# # # # # # # # E #
⻚码：1/4
东 北 ⼤ 学东北⼤学计算机学院
算法设计与分析课程设计任务书
2024-2025春季学期
需求：（1）迷宫⽆孤⽴区域且存在唯⼀通路。（2）⽀持多种尺⼨。
最⼩尺⼨为 7 × 7 ，只能容纳较少机关 / 资源 / 陷阱。理想尺⼨为
15×15，可设置较多机关/资源/陷阱，增加策略性。（3）起点、终
点、资源、陷阱、机关、BOSS均随机分布。（4）可选：可视化⽣成
过程（动画）。
2. 采⽤动态规划进⾏资源收集路径规划（作为实时策略的⾦标准）
任务：计算从起点到终点的最优资源收集路径，避开陷阱，优先拾取
资源。例如：状态dp[i][j]表⽰⾛到坐标(i,j)时的最⼤资源值。
输⼊：迷宫矩阵、资源分布{(x, y) : value}（例如⾦币=5）、陷阱位
置[(x, y)]（可设置陷阱=-3）。
输出：最⼤资源值、最优路径序列。
需求：路径可视化（可选）。
3. 采⽤贪⼼算法设计实时资源拾取策略
任务：玩家视野受限于周围3 × 3区域，每次移动时优先选择视野内
“性价⽐”（例如单位距离收益最⼤）最⾼的资源，重复直⾄⽆资源可
拾取。
输⼊：当前玩家位置、3 × 3视野内的资源信息。
输出：资源拾取路径。
4. 采⽤回溯法解谜关卡
输⼊：⼀个3位密码锁的位置和线索（如每位密码为素数且不重复、
或第1位是偶数等）。
输出：密码。
5. 采⽤分⽀限界设计BOSS战策略优化
任务：在限定回合内击败BOSS，寻找最⼩代价的技能序列。
输⼊：玩家剩余资源、BOSS⾎量、玩家可⽤技能（例如，普通攻
击：伤害5、⽆冷却；⼤招：伤害10，冷却2回合）。
⻚码：2/4
东 北 ⼤ 学东北⼤学计算机学院
算法设计与分析课程设计任务书
2024-2025春季学期
输出：最⼩回合数的技能序列。
例如，（1）节点状态可包括当前BOSS⾎量、玩家剩余资源、已⽤回
合数。（2）代价函数：f(n) = 已⽤回合数 + 预估剩余回合数
（BOSS⾎量/玩家平均伤害）。（3）剪枝策略：丢弃代价超过当前
最优解的节点。
三. 验收标准
评分项
权重
要求
算法正确性
游戏可玩性
代码质量
报告与答辩
30%
25%
20%
25%
分治⽣成迷宫连通、动态规划路径最优、贪⼼决策合理、
回溯与分⽀限界剪枝⾼效等。
迷宫探险流程完整，战⽃与解谜环节有挑战性。
模块化设计，注释清晰，可扩展性强。
按报告撰写要求完成报告，答辩展⽰游戏运⾏。
四. 报告撰写要求
1. 模板：报告采⽤《计算机学报》模板http://cjc.ict.ac.cn/wltg/
new/submit/index.asp，分为word模板和Latex模板。⿎励使⽤
Latex 模板。科技排版系统 Tex L i v e 的下载地址为 http://
mirrors.ustc.edu.cn/CTAN/systems/texlive/Images/。
2. 页数：基于《计算机学报》模板所制定的格式（⾏间距、段间
距、字号等），报告的页数应不低于7页。
3. 内容：（1）给出各个算法的递归函数、状态转移⽅程或选择策
略；（2）给出各个算法的时间与空间复杂度分析过程；（3）给
出各个算法中具有创新性的部分；（4）以图表形式展⽰实验结
果，包括迷宫连通性测试、路径收益对⽐（即资源收集路径规划
与实时资源拾取路径）、解谜尝试次数统计等。
⻚码：3/4
东 北 ⼤ 学东北⼤学计算机学院
算法设计与分析课程设计任务书
2024-2025春季学期
五. 成果提交要求
1. 成果包括：（1）报告（提交pdf版）；（2）游戏运⾏录屏；
（3）代码；（4）⼩组成员⼯作量占⽐（每位成员电⼦签名）。
2. 提交时间：截⽌第21周周三（7⽉23⽇）。
3. 提交格式：⼀个压缩包，⽂件名为“专业班级-组长姓名”。
⻚码：4/4
东 北 ⼤ 学