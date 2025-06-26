const vm = new Vue({
    el: '#app',
    data: {
        message: 'Hello Vue!',
        board: [
            ["WALL", "WALL", "WALL", "WALL", "WALL"],
            ["WALL", "PATH", "PATH", "GOAL", "WALL"],
            ["WALL", "HERO", "PATH", "PATH", "WALL"],
            ["WALL", "PATH", "PATH", "PATH", "WALL"],
            ["WALL", "WALL", "WALL", "WALL", "WALL"]
        ],
        validSize: 10,
        lastCellInfo: null,
        blockTypes: {
            'WALL': '1',
            'PATH': '⬜',
            'HERO': '🦸',
            'GOAL': '🎯',
            'START': '🏁',
            'SKILL': ['🗡️', '⚔️', '🏹', '💣',]
            // Add more block types as needed
        },
        heroPos: {
            x: 2,
            y: 1
        },
        paths: []
    },
    created() {
        this.refresh();
    },

    methods: {
        refresh() {
            fetch("http://localhost:8080/fullUpdate")
                .then(
                    res => {
                        // console.log(res);
                        res.json().then(data => {
                            // console.log(data);
                            this.board = data.maze.board;
                            this.heroPos = {x: data["position"].x, y: data["position"].y};

                            console.log("data[position] is ", data["position"]);
                            console.log("data is when mounted", data);
                            console.log("this.heroPos is ", this.heroPos);
                        });
                    }
                );
        },
        getCell(x, y) {
            return this.$refs[`cell_${x}_${y}`][0];
        },
        recover() {
            if (this.lastCellInfo !== null) {
                const x1 = this.lastCellInfo.pos.x;
                const y1 = this.lastCellInfo.pos.y;
                let lastCell = this.$refs[`cell_${x1}_${y1}`][0];
                // lastCell.inn
                this.$set(this.board[x1], y1, this.lastCellInfo.block);
                lastCell.style.backgroundColor = "";
                console.log("recovered last cell", this.lastCellInfo);
                this.paths[this.paths.length - 1].forEach(e => {
                    this.getCell(e.x, e.y).style.backgroundColor = "";
                });
            }
        },
        notifyBackend(data) {
            // const data = {
            //     position: this.heroPos,
            //     msg: "notify from client"
            // };
            fetch("http://localhost:8080/notify", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            })
                .then(res => {
                    if (res.ok) {
                        console.log("Notify backend successfully");
                    } else {
                        console.error("Failed to notify backend");
                    }
                })
                .catch(err => console.error("Error notifying backend:", err));
        },
        handleNextStep() {
            fetch("http://localhost:8080/nextPointWithPath")
                .then(
                    (res) => {
                        res.json().then(data => {
                            let x = data["target"].x;
                            let y = data["target"].y;
                            let path = data["path"];
                            console.log("data is ", data);
                            const oldBlockType = this.board[x][y];
                            let cell = oldBlockType;
                            // 获取位置为 (x,y) 的单元格 DOM 元素
                            // ! update new info for next recover
                            // judge whether the boss is on the path
                            if (data["bossOnPath"] === true && confirm("The boss is on the path, do you want to fight with it?")) {
                                const bossPosition = data["bossPosition"];
                                this.heroPos = bossPosition;
                                this.notifyBackend({
                                    position: this.heroPos,
                                });
                                // slice the path until the boss position
                                path = path.slice(0, path.findIndex(p => p.x === bossPosition.x && p.y === bossPosition.y) + 1);
                                x = bossPosition.x;
                                y = bossPosition.y;
                            } else {
                                this.heroPos = {x: x, y: y};
                            }
                            // do normal update
                            this.paths = [...this.paths, path];
                            this.lastCellInfo = {
                                pos: this.heroPos,
                                block: oldBlockType
                            };
                            path.forEach(e => {
                                this.getCell(e.x, e.y).style.backgroundColor = "rgba(33, 241, 116, 0.42)";
                                if (x === e.x && y === e.y &&this.board[e.x][e.y] === "GOLD" || this.board[e.x][e.y] === "SKILL") {
                                    this.$set(this.board[e.x], e.y, "PATH");
                                }
                            });
                            let cellElement = this.getCell(x, y);
                            cellElement.style.backgroundColor = "pink";
                        });
                    }
                );
        }
    }
});