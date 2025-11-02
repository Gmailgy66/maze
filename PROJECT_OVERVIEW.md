# 迷宫游戏项目 (Maze Adventure Game)

## 项目概述

这是一个综合性的迷宫冒险游戏项目，包含迷宫生成、路径规划、谜题解决和战斗系统等多个模块。项目使用Java Spring Boot框架开发Web应用，并配有Python脚本用于迷宫可视化。

## 技术栈

### 后端技术
- **Java 21** - 主要编程语言
- **Spring Boot 3.5.3** - Web应用框架
- **Thymeleaf** - 模板引擎
- **Maven** - 项目构建工具
- **Lombok** - 代码简化工具

### 前端技术
- **Vue.js** - 前端JavaScript框架
- **HTML5/CSS3** - 页面展示
- **JavaScript** - 交互逻辑

### Python工具
- **NumPy** - 数值计算
- **Matplotlib** - 可视化库
- **JSON** - 数据交换格式

## 项目结构

```
maze/
├── game/                          # 主游戏应用（Spring Boot）
│   ├── src/main/java/
│   │   └── com/caicai/game/
│   │       ├── GameApplication.java      # Spring Boot启动类
│   │       ├── Game.java                 # 游戏核心逻辑
│   │       ├── maze/                     # 迷宫模块
│   │       │   ├── Maze.java            # 迷宫数据结构
│   │       │   ├── MazeFactory.java     # 迷宫生成器（递归分割算法）
│   │       │   ├── BlockType.java       # 方块类型枚举
│   │       │   └── PointUtil.java       # 坐标工具
│   │       ├── combat/                   # 战斗系统
│   │       │   └── Combat.java          # Boss战斗逻辑
│   │       ├── quiz/                     # 谜题系统
│   │       │   ├── Question.java        # 问题类
│   │       │   └── PasswordSolver.java  # 密码破解器
│   │       ├── role/                     # 角色系统
│   │       │   └── Skill.java           # 技能系统
│   │       ├── common/                   # 通用工具
│   │       │   ├── PathFinder.java      # 路径查找接口
│   │       │   ├── PathSolve.java       # 路径求解（动态规划）
│   │       │   ├── Point.java           # 坐标点类
│   │       │   └── Result.java          # 统一返回结果
│   │       ├── controller/               # Web控制器
│   │       │   └── Router.java          # 路由处理
│   │       └── conf/                     # 配置类
│   │           └── GameConf.java        # 游戏配置
│   ├── src/main/resources/
│   │   ├── static/                      # 静态资源
│   │   │   ├── game.html               # 游戏主界面
│   │   │   ├── game.js                 # 游戏逻辑JS
│   │   │   ├── combat.html             # 战斗界面
│   │   │   └── index.html              # 首页
│   │   ├── templates/                   # Thymeleaf模板
│   │   ├── boss_battle.json            # Boss战斗配置
│   │   └── password_data.json          # 密码数据
│   ├── data/
│   │   ├── password/                    # 密码题库（100个JSON文件）
│   │   └── boss/                        # Boss数据
│   └── pom.xml                          # Maven配置文件
├── maze.py                              # Python迷宫生成动画脚本
├── mazeTest.py                          # Python迷宫验证脚本
├── maze.json                            # 迷宫数据文件
└── readme.md                            # 项目说明

```

## 核心功能模块

### 1. 迷宫生成系统 (MazeFactory.java)

**算法**: 递归分割算法 (Recursive Division Algorithm)

**功能特点**:
- 动态生成指定大小的迷宫（奇数大小，如15x15、21x21等）
- 使用递归分割算法创建完美迷宫（无环路，任意两点间有唯一路径）
- 自动添加特殊元素：
  - **起点 (S)** - 游戏开始位置
  - **终点 (E)** - 游戏目标位置
  - **金币 (G)** - 可收集资源（约30%通道）
  - **陷阱 (T)** - 危险区域（约10%通道）
  - **Boss (B)** - 需要战斗的Boss位置
  - **锁 (L)** - 需要解谜才能通过
  - **墙壁 (#)** - 不可通行区域

**算法流程**:
1. 初始化全空迷宫（保留边框）
2. 递归分割：
   - 在偶数位置画横线和竖线
   - 在四段墙上各开一个奇数位置的通道
   - 随机选择一个通道不打开（保持连通性）
   - 对四个分割区域递归执行
3. 后处理：确保起点、终点、Boss点存在

### 2. 路径规划系统

#### 动态规划路径查找 (PathSolve.java)
- **算法**: 动态规划 + 广度优先搜索
- **功能**: 找到收集所有金币并到达终点的最优路径
- **优化**: 考虑金币收集和路径长度的平衡

#### 贪心路径查找 (GreedyPathFinder.java)
- **算法**: 贪心策略
- **功能**: 快速找到近似最优路径
- **特点**: 速度快，适合实时计算

### 3. 战斗系统 (Combat.java)

**功能**: 回合制Boss战斗系统

**战斗机制**:
- 支持多个Boss同时战斗
- 玩家技能系统：
  - 不同技能有不同的伤害值
  - 冷却时间机制（Cooldown）
  - 单体攻击和群体攻击
- Boss血量系统：
  - 多Boss独立血量追踪
  - 击败条件：所有Boss血量归零
- 策略优化：
  - 使用优先队列寻找最优战斗策略
  - 最小回合数求解
  - 记录最佳技能使用顺序

**配置文件**: `boss_battle.json`
```json
{
  "B": [Boss血量数组],
  "PlayerSkills": [
    {
      "damage": 伤害值,
      "cooldown": 冷却回合,
      "target": 目标类型（single/all）
    }
  ]
}
```

### 4. 谜题系统 (PasswordSolver.java)

**功能**: 密码破解谜题

**算法**:
- 回溯法求解密码
- 使用SHA-256哈希验证
- 根据线索约束搜索空间
- 三种搜索策略：
  1. 从0开始递增
  2. 从9开始递减
  3. 按线索优先级搜索
- 返回最少尝试次数

**数据格式**: 100个密码题目文件 (pwd_001.json ~ pwd_100.json)
```json
{
  "C": [
    [位置, 数字],     // 确定位置的数字
    [-1, -1, 数字],   // 数字存在但位置未知
    [位置1, 位置2]    // 两个位置的数字关系
  ],
  "L": "SHA256哈希值"  // 目标密码的哈希
}
```

### 5. Python可视化工具

#### maze.py - 迷宫生成动画
**功能**:
- 读取maze.txt文件（Java生成的迷宫构建过程）
- 使用Matplotlib生成迷宫构建过程动画
- 导出为GIF和MP4格式
- 高亮显示每步操作的坐标点
- 可配置帧率、分辨率、比特率

**输出**:
- `maze_generation.gif` - GIF动画
- `maze_generation.mp4` - 高清视频

#### mazeTest.py - 迷宫验证工具
**功能**:
- 读取maze.json文件验证迷宫结构
- 检查终点可达性
- 检查路径唯一性（无环路）
- 检测孤立区域
- 使用BFS和DFS算法验证迷宫连通性

**验证项目**:
1. 起点到终点可达性
2. 是否存在唯一通路
3. 是否存在环路
4. 是否存在孤立区域

### 6. Web界面

#### 游戏主界面 (game.html)
**功能**:
- 迷宫可视化显示
- 实时游戏状态更新
- 角色移动控制
- 得分和金币统计
- 迷宫大小配置（5-99）
- 文件上传功能（支持自定义迷宫）
- 默认迷宫加载
- 路径提示（DP算法/贪心算法）

**游戏统计**:
- 得分系统
- 金币收集计数
- 游戏时间记录

#### 战斗界面 (combat.html)
**功能**:
- Boss血量显示
- 技能列表展示
- 冷却时间可视化
- 战斗日志记录
- 最优策略计算

### 7. REST API接口

#### 游戏控制接口
- `GET /game?size={size}` - 初始化新游戏
- `POST /uploadMaze` - 上传自定义迷宫
- `GET /loadMaze` - 加载默认迷宫
- `GET /fullUpdate` - 获取完整游戏状态

#### 路径规划接口
- `GET /dpPath` - 获取动态规划路径
- `GET /greedyPath` - 获取贪心算法路径

#### 战斗系统接口
- `GET /combat` - 进入战斗页面
- `GET /startCombat` - 开始战斗
- `GET /quiz` - 获取谜题

## 游戏配置

### GameConf.java
```java
@ConfigurationProperties(prefix = "game")
public class GameConf {
    private Integer size;    // 迷宫大小（默认15）
    private Integer level;   // 难度等级（影响资源和陷阱数量）
}
```

### application.yml
```yaml
game:
  size: 15      # 迷宫大小
  level: 10     # 难度级别
server:
  port: 8080    # Web服务器端口
```

## 数据结构

### BlockType枚举
```java
enum BlockType {
    PATH(" "),      // 通道
    WALL("#"),      // 墙壁
    START("S"),     // 起点
    EXIT("E"),      // 终点
    GOLD("G"),      // 金币
    TRAP("T"),      // 陷阱
    BOSS("B"),      // Boss
    LOCKER("L");    // 锁
}
```

### Point类
```java
class Point {
    int x;          // x坐标
    int y;          // y坐标
}
```

### Maze类
```java
class Maze {
    BlockType[][] board;    // 迷宫二维数组
    Point START;            // 起点坐标
    Point EXIT;             // 终点坐标
    Point bossPoint;        // Boss坐标
    Point LOCKER;           // 锁坐标
    Set<Point> paths;       // 所有通道坐标集合
}
```

## 运行方式

### 启动Java应用
```bash
cd game
mvn clean package
mvn spring-boot:run
```

访问: `http://localhost:8080/game?size=15`

### 运行Python可视化
```bash
# 首先启动Java应用生成maze.txt
python maze.py          # 生成动画

# 验证迷宫
python mazeTest.py      # 验证迷宫结构
```

## 项目特色

### 1. 算法丰富性
- 递归分割迷宫生成算法
- 动态规划路径优化
- 贪心算法快速求解
- 回溯法谜题求解
- BFS/DFS图遍历算法

### 2. 系统完整性
- 完整的游戏循环
- 前后端分离架构
- RESTful API设计
- 配置化系统

### 3. 可扩展性
- 模块化设计
- 支持自定义迷宫
- 可配置难度
- 插件化战斗系统

### 4. 可视化支持
- Web界面实时展示
- Python动画生成
- 调试工具完善

## 开发说明

### 已完成功能
✅ 谜题修改
✅ 战斗修改
✅ 迷宫生成算法
✅ 路径规划系统
✅ Web界面
✅ 可视化工具

### 技术亮点
1. **迷宫生成**: 使用递归分割算法保证迷宫质量（无环、唯一路径）
2. **路径优化**: 多种算法对比（DP vs 贪心）
3. **战斗系统**: 回合制策略优化，最小回合数求解
4. **谜题系统**: 密码破解，回溯搜索优化
5. **全栈实现**: Java后端 + Vue.js前端 + Python工具链

## 项目依赖

### Java依赖
- spring-boot-starter-web
- spring-boot-starter-thymeleaf
- spring-boot-devtools
- lombok
- json (org.json)
- commons-lang3

### Python依赖
- numpy
- matplotlib
- json (标准库)

## 总结

这是一个**功能完整的Web迷宫冒险游戏**，整合了：
- 🎮 **游戏系统**: 迷宫探险、收集金币、Boss战斗
- 🧩 **谜题系统**: 密码破解挑战
- 🗺️ **迷宫生成**: 程序化生成完美迷宫
- 🛤️ **路径规划**: 智能路径优化算法
- 💻 **Web应用**: 完整的前后端实现
- 📊 **可视化**: Python动画和验证工具

项目展示了多种算法和数据结构的实际应用，适合作为算法学习、游戏开发和Web开发的综合实践项目。
