import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib.colors import ListedColormap
import os


def read_maze_file(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()

    n = int(lines[0].strip())  # 迷宫大小
    if n % 2 == 0:
        n = n + 1
    coordinates_groups = []

    group = []

    for line in lines[1:]:
        line = line.strip()
        if not line:  # 跳过空行
            continue

        if '(' in line and ')' in line:
            x, y = map(int, line.replace('(', '').replace(')', '').split(','))
            group.append((x, y))

            if len(group) == 4:  # 每组包含4个坐标
                coordinates_groups.append(group)
                group = []

    return n, coordinates_groups


def initialize_maze(n):
    # 初始化迷宫：0表示空地，1表示墙
    maze = np.ones((n, n))  # 全部初始化为墙

    # 将内部设为空地（保留边框）
    maze[1:n - 1, 1:n - 1] = 0

    return maze


def create_maze_animation(save_gif=True, save_mp4=True, output_prefix='maze_generation',
                          fps=5, dpi=200, bitrate=6000):
    # 读取迷宫数据
    n, coord_groups = read_maze_file('maze.txt')

    # 初始化迷宫
    maze = initialize_maze(n)
    maze_states = [maze.copy()]
    point = set()

    # 为每组坐标创建一个迷宫状态
    for group in coord_groups:
        new_maze = maze_states[-1].copy()
        # 第一个坐标表示需要设为墙的行和列
        x, y = group[0]  # 获取第一个坐标

        # 横向墙壁（向左延伸）
        y_ = y
        while y_ >= 0 and new_maze[x][y_] != 1:
            new_maze[x][y_] = 1
            y_ = y_ - 1

        # 横向墙壁（向右延伸）
        y_ = y + 1
        while y_ < n and new_maze[x][y_] != 1:
            new_maze[x][y_] = 1
            y_ = y_ + 1

        # 纵向墙壁（向上延伸）
        x_ = x - 1
        while x_ >= 0 and new_maze[x_][y] != 1:
            new_maze[x_][y] = 1
            x_ = x_ - 1

        # 纵向墙壁（向下延伸）
        x_ = x + 1
        while x_ < n and new_maze[x_][y] != 1:
            new_maze[x_][y] = 1
            x_ = x_ + 1

        # 为当前组的所有坐标创建通道
        for x, y in group[1:]:
            if 0 <= x < n and 0 <= y < n:
                new_maze[x, y] = 0  # 将墙改为通道
                point.add((x, y))  # 添加到点集合中

        maze_states.append(new_maze)

    # 创建高分辨率动画
    fig, ax = plt.subplots(figsize=(12, 12), dpi=dpi)
    cmap = ListedColormap(['white', 'black'])  # 0: 空地, 1: 墙
    im = ax.imshow(maze_states[0], cmap=cmap, interpolation='nearest')

    # 添加网格线
    ax.grid(which='major', color='gray', linestyle='-', linewidth=0.5, alpha=0.3)
    ax.set_xticks(np.arange(-0.5, n, 1))
    ax.set_yticks(np.arange(-0.5, n, 1))
    ax.set_xticklabels([])
    ax.set_yticklabels([])

    # 更新函数
    def update(frame):
        im.set_array(maze_states[frame])

        # 清除之前的高亮点
        for artist in ax.collections:
            artist.remove()

        # 高亮显示当前更改的坐标
        if frame > 0:
            for x, y in coord_groups[frame - 1]:
                ax.scatter(y, x, color='red', s=80, marker='o',
                           edgecolors='darkred', linewidths=1.5, alpha=0.8)

        ax.set_title(f'迷宫生成 - 步骤 {frame}/{len(maze_states) - 1}', fontsize=14)
        return [im]

    # 创建动画对象
    ani = animation.FuncAnimation(
        fig, update, frames=len(maze_states),
        interval=120, blit=False  # 间隔120ms，更流畅的播放
    )

    # 保存为MP4视频
    if save_mp4:
        mp4_filename = f"{output_prefix}.mp4"
        print(f"正在保存MP4视频: {mp4_filename}...")
        try:
            Writer = animation.writers['ffmpeg']
            writer = Writer(fps=fps, metadata=dict(artist='MazeGenerator'), bitrate=bitrate)
            ani.save(mp4_filename, writer=writer, dpi=dpi)
            print(f"MP4视频已成功保存: {mp4_filename}")
        except Exception as e:
            print(f"保存MP4失败: {e}")
            print("请确认已安装ffmpeg")

    # 保存为GIF
    if save_gif:
        gif_filename = f"{output_prefix}.gif"
        print(f"正在保存GIF: {gif_filename}...")
        try:
            ani.save(gif_filename, writer='pillow', fps=fps, dpi=dpi // 2)  # GIF文件较大，降低dpi
            print(f"GIF已成功保存: {gif_filename}")
        except Exception as e:
            print(f"保存GIF失败: {e}")

    plt.close()
    print("动画生成完毕")

    return ani  # 返回动画对象，以便后续可能的使用


if __name__ == "__main__":
    # 执行动画生成并同时保存为GIF和MP4
    create_maze_animation(
        save_gif=True,  # 保存为GIF
        save_mp4=True,  # 保存为MP4
        output_prefix='maze_generation',  # 输出文件名前缀
        fps=5,  # 帧率，更高的值播放速度更快
        dpi=200,  # 分辨率，更高的值图像更清晰
        bitrate=6000  # 比特率，更高的值视频质量更好
    )