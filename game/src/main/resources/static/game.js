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
            'WALL': '🧱',
            'PATH': '⬜',
            'HERO': '🦸',
            'GOAL': '🎯',
            'START': '🏁',
            'GOLD': '🪙',
            'SKILL': ['🗡️', '⚔️', '🏹', '💣']
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
        isLoading: false,
        error: null,
        curInd: 0
    },

    created() {
        this.refresh();
    },

    methods: {
        async refresh() {
            this.isLoading = true;
            this.error = null;

            try {
                const response = await fetch("http://localhost:8080/fullUpdate");

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();

                this.board = data.maze?.board || this.board;
                this.heroPos = {
                    x: data.position?.x || this.heroPos.x,
                    y: data.position?.y || this.heroPos.y
                };

                console.log("Game data refreshed:", data);
            } catch (error) {
                console.error("Failed to refresh game data:", error);
                this.error = "Failed to load game data. Please try again.";
            } finally {
                this.isLoading = false;
            }
        },

        getCell(x, y) {
            const cellRef = this.$refs[`cell_${x}_${y}`];
            return cellRef && cellRef[0] ? cellRef[0] : null;
        },

        async handleSolve() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await fetch("http://localhost:8080/nextPointWithPath");

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                this.path = data.path || [];
                this.segments = [];
                console.log("Path data received:", this.path);
                // this.processPathSegments();
                // console.log("Path solved:", { path: this.path, segments: this.segments });
            } catch (error) {
                console.error("Failed to solve path:", error);
                this.error = "Failed to solve the maze. Please try again.";
            } finally {
                this.isLoading = false;
            }
        },

        processPathSegments() {
            if (!this.path.length) return;

            this.segments = [[]];

            this.path.forEach(point => {
                if (this.isValidPosition(point.x, point.y) &&
                    this.board[point.x][point.y] === "GOLD") {
                    this.segments.push([]);
                }

                if (this.segments.length > 0) {
                    this.segments[this.segments.length - 1].push(point);
                }
            });
            // Remove empty segments
            this.segments = this.segments.filter(segment => segment.length > 0);
            console.log("Processed path segments:", this.segments);
        },

        isValidPosition(x, y) {
            return x >= 0 && x < this.board.length &&
                y >= 0 && y < this.board[0].length;
        },

        async getRes(url) {
            if (!url) {
                console.warn("URL is required for getRes method");
                return null;
            }

            try {
                const response = await fetch(url);

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                return await response.json();
            } catch (error) {
                console.error(`Failed to fetch data from ${url}:`, error);
                return null;
            }
        },

        resetGame() {
            // this.path = [];
            // this.segments = [];
            this.steps = 0;
            this.score = 0;
            this.error = null;
            this.refresh();
        },
        highlightLine(ind) {
            this.segments[ind].forEach(point => {
                const cell = this.getCell(point.x, point.y);
                if (cell) {
                    cell.style.backgroundColor = 'yellow';
                }
            });
        },
        recoverLastLine(ind) {
            if (ind < 0 || ind >= this.segments.length) {
                console.warn("Invalid segment index");
                return;
            }
            this.segments[ind].forEach(point => {
                const cell = this.getCell(point.x, point.y);
                if (cell) {
                    cell.style.backgroundColor = '';
                }
            });
        },
        stepOne() {
            if (this.curInd < 0 || this.curInd >= this.segments.length) {
                console.warn("Current index is out of bounds");
                return;
            }
            ++this.curInd;
            this.highlightLine(this.curInd);
            this.recoverLastLine(this.curInd - 1);
        }
    },

    computed: {
        gameStatus() {
            if (this.isLoading) return "Loading...";
            if (this.error) return "Error";
            return "Ready";
        },

        totalPathLength() {
            return this.path.length;
        }
    }
});