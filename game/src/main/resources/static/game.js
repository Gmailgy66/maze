const vm = new Vue({
    el: '#app',
    data: {
        message: 'Hello Vue!',
        board: [],
        validSize: 10,
        lastCellInfo: null,
        blockTypes: {
            'WALL': '🧱',
            'PATH': '⬜',
            'HERO': '🧙‍♂️',
            'GOAL': '🎯',
            'START': '🏁',
            'GOLD': '🪙',
            'SKILL': '💣',
            'TRAP': '☠️',
            'EXIT': '🔚',
            'BOSS': '🦖',
            'LOCKER': '🔞'
        },
        heroPos: {
            x: 0,
            y: 0
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
        curInd: -1,
        currentLevel: 1,
        stepCnt: 0,
        boardCopy: []
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

                // Safely update board
                if (data.maze && Array.isArray(data.maze.board)) {
                    this.board = data.maze.board;
                } else {
                    console.warn("No valid board data received");
                    this.board = this.getDefaultBoard();
                }

                // Safely update hero position
                if (data.position && typeof data.position.x === 'number' && typeof data.position.y === 'number') {
                    this.heroPos = {
                        x: data.position.x,
                        y: data.position.y
                    };
                } else {
                    this.findHeroPosition();
                }

                // Update other game data
                this.score = data.score || this.score;
                this.steps = data.steps || this.steps;
                this.boardCopy = this.board
                console.log("Game data refreshed:", data);
            } catch (error) {
                console.error("Failed to refresh game data:", error);
                this.error = "Failed to load game data. Using default board.";
                this.board = this.getDefaultBoard();
                this.findHeroPosition();
            } finally {
                this.isLoading = false;
            }
        },

        getDefaultBoard() {
            return [
                ["WALL", "WALL", "WALL", "WALL", "WALL"],
                ["WALL", "START", "PATH", "GOAL", "WALL"],
                ["WALL", "PATH", "PATH", "PATH", "WALL"],
                ["WALL", "PATH", "PATH", "PATH", "WALL"],
                ["WALL", "WALL", "WALL", "WALL", "WALL"]
            ];
        },

        findHeroPosition() {
            // Find hero position in board or use START position
            for (let i = 0; i < this.board.length; i++) {
                for (let j = 0; j < this.board[i].length; j++) {
                    if (this.board[i][j] === 'HERO' || this.board[i][j] === 'START') {
                        this.heroPos = {x: i, y: j};
                        return;
                    }
                }
            }
            // Default position if not found
            this.heroPos = {x: 1, y: 1};
        },

        getCell(x, y) {
            const cellRef = this.$refs[`cell_${x}_${y}`];
            return cellRef && cellRef[0] ? cellRef[0] : null;
        },

        getCellClass(x, y) {
            const classes = ['maze-cell'];

            // Add hero position class
            if (this.heroPos.x === x && this.heroPos.y === y) {
                classes.push('hero-position');
            }

            // Add path highlight class if this cell is in current path segment
            if (this.isInCurrentSegment(x, y)) {
                classes.push('path-highlight');
            }

            return classes.join(' ');
        },

        isInCurrentSegment(x, y) {
            if (this.curInd < 0 || this.curInd >= this.segments.length) return false;

            return this.segments[this.curInd].some(point => point.x === x && point.y === y);
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
                this.curInd = -1;

                console.log("Path data received:", this.path);
                this.processPathSegments();

                console.log("Path solved:", {path: this.path, segments: this.segments});
            } catch (error) {
                console.error("Failed to solve path:", error);
                this.error = "Failed to solve the maze. Please try again.";
            } finally {
                this.isLoading = false;
            }
        },

        processPathSegments() {
            if (!this.path.length) {
                console.warn("No path to process");
                return;
            }

            this.segments = [[]];

            this.path.forEach(point => {
                // Create new segment when encountering GOLD
                // The GOLD should end a segment
                this.segments[this.segments.length - 1].push(point);
                if (this.isValidPosition(point.x, point.y) &&
                    this.board[point.x][point.y] === "GOLD") {
                    this.segments.push([]);
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

        async updateBoardSize() {
            if (this.validSize < 5 || this.validSize > 30) {
                this.error = "Board size must be between 5 and 30";
                return;
            }

            try {
                const response = await fetch(`http://localhost:8080/updateSize?size=${this.validSize}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    await this.refresh();
                } else {
                    this.error = "Failed to update board size";
                }
            } catch (error) {
                console.error("Failed to update board size:", error);
                this.error = "Failed to update board size";
            }
        },

        resetGame() {
            this.segments = [];
            this.path = [];
            this.steps = 0;
            this.score = 0;
            this.error = null;
            this.curInd = -1;
            this.clearOldStyle();
            this.refresh();

        },

        clearOldStyle() {
            // Clear all cell highlights
            this.board.forEach((row, x) => {
                row.forEach((_, y) => {
                    const cell = this.getCell(x, y);
                    if (cell) {
                        cell.style.backgroundColor = '';
                        // cell.innerText = '';
                    }
                });
            });
        },

        highlightSegment(segmentIndex) {
            if (segmentIndex < 0 || segmentIndex >= this.segments.length) {
                console.warn("Invalid segment index:", segmentIndex);
                return;
            }
            // create a random color for the segment highlight
            const randomColor = `hsl(${Math.random() * 360}, 100%, 75%)`;
            this.segments[segmentIndex].forEach(point => {
                this.stepCnt++;
                if (this.board[point.x][point.y] === "GOLD") {
                    this.board[point.x][point.y] = "PATH"; // Change GOLD to PATH for highlighting
                }
                this.heroPos = {x: point.x, y: point.y}; // Update hero position to current segment point
                const cell = this.getCell(point.x, point.y);
                if (cell) {
                    // cell.style.backgroundColor = 'yellow';
                    cell.style.backgroundColor=randomColor;
                    cell.style.border = '2px solid orange';
                    // cell.innerText = this.stepCnt;
                }
            });
        },

        clearSegmentHighlight(segmentIndex) {
            return;
            if (segmentIndex < 0 || segmentIndex >= this.segments.length) {
                return;
            }

            this.segments[segmentIndex].forEach(point => {
                const cell = this.getCell(point.x, point.y);
                if (cell) {
                    cell.style.backgroundColor = '';
                    cell.style.border = '';
                }
            });
        },

        stepOne() {
            console.log("Stepping one segment forward...");
            if (this.segments.length === 0) {
                this.error = "No path segments available. Solve the maze first.";
                return;
            }

            // Clear previous highlight
            if (this.curInd >= 0) {
                this.clearSegmentHighlight(this.curInd);
            }
            // Move to next segment
            this.curInd++;
            if (this.curInd >= this.segments.length) {
                this.curInd = 0; // Loop back to start
            }

            this.highlightSegment(this.curInd);
            // console.log(`Stepped to segment ${this.curInd + 1} of ${this.segments.length}`);
            console.log(`Stepped to segment ${this.curInd + 1} of ${this.segments.length}`, this.segments[this.curInd]);
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