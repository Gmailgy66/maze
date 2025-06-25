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
        }
    },
    created() {
        this.refresh();
    },

    methods: {
        getBlockDisplay(block) {
            console.log("getBlockDisplay called with block:", block);
            return this.blockTypes[block.toUpperCase()] || block;
        },
        refresh() {
            fetch("http://localhost:8080/fullUpdate")
                .then(
                    res => {
                        // console.log(res);
                        res.json().then(data => {
                            // console.log(data);
                            this.board = data.maze.board;
                            console.log("board is ", this.board);
                        });
                    }
                );
        },
        handleNextStep() {
            fetch("http://localhost:8080/nextStep")
                .then(
                    (res) => {
                        res.json().then(data => {
                            let x = data["position"].x;
                            let y = data["position"].y;
                            // console.log("data is ", data)
                            const oldBlockType = this.board[x][y];
                            let cell = oldBlockType;
                            // 获取位置为 (x,y) 的单元格 DOM 元素
                            let cellElement = this.$refs[`cell_${x}_${y}`][0];

                            if (this.lastCellInfo !== null) {
                                const x1 = this.lastCellInfo.pos.x;
                                const y1 = this.lastCellInfo.pos.y;
                                let lastCell = this.$refs[`cell_${x1}_${y1}`][0];
                                // lastCell.inn
                                this.$set(this.board[x1], y1, this.lastCellInfo.block);
                                lastCell.style.backgroundColor = "";
                                console.log("recovered last cell", this.lastCellInfo);
                            }
                            this.lastCellInfo = {
                                pos: {x: x, y: y},
                                block: oldBlockType
                            };
                            this.$set(this.board[x], y, "HERO");
                            cellElement.style.backgroundColor = "red";
                        });
                    }
                );
        }
    }
});