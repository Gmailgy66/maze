const vm = new Vue({
    el: '#app',
    data: {
        message: 'Hello Vue!',
        board: [
            ["WALL", "WALL", "WALL", "WALL", "WALL"],
            ["WALL", "PATH  ", "PATH", "GOAL", "WALL"],
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
        score: 0,
        skills: [],
        boss: [],
        path: [],
        segments: [],
        steps: 0,
        fullPath: [],
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
                            this.heroPos = { x: data["position"].x, y: data["position"].y };
                            console.log("data is when mounted", data);
                        });
                    }
                );
        },
        getCell(x, y) {
            return this.$refs[`cell_${x}_${y}`][0];
        },
        // recover() {
        //     if (this.lastCellInfo !== null) {
        //         const x1 = this.lastCellInfo.pos.x;
        //         const y1 = this.lastCellInfo.pos.y;
        //         let lastCell = this.$refs[`cell_${x1}_${y1}`][0];
        //         // lastCell.inn
        //         this.$set(this.board[x1], y1, this.lastCellInfo.block);
        //         lastCell.style.backgroundColor = "";
        //         console.log("recovered last cell", this.lastCellInfo);

        //     }
        // },
        handleSolve() {
            fetch("http://localhost:8080/nextPointWithPath")
                .then(res => res.json().then(data => {
                    console.log("data is", data);
                    console.log("this is ", this);
                    this.path = data.path;
                    this.path.forEach(p => {
                        console.log("p is ", p);
                        if (this.board[p.x][p.y] === "GOLD") {
                            this.segments.add([]);
                        }
                        this.segments[this.segments.length - 1].add(p);
                    });
                    console.log("segments are ", this.segments);
                }));
        },
        getRes(url) {

        },
    },
}
);