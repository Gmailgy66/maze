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
            'LOCKER': '🔞',
            'GOLD_COLLECTED': '💰',  // Mask for collected gold
            'SKILL_COLLECTED': '⚡',  // Mask for collected skill
            'TRAP_TRIGGERED': '💀'   // Mask for triggered trap
        },
        heroPos: {
            x: 0,
            y: 0
        },
        score: 0,
        scoreHistory: [], // Track score changes for display
        collectedItems: new Set(), // Track collected items by position key
        goldCollected: 0,
        trapsTriggered: 0,
        skillsCollected: 0,
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
        boardCopy: [],
        // Auto-play functionali
        isAutoPlaying: false,
        autoPlaySpeed: 50, // milliseconds between steps
        autoPlayTimer: null,
        animationDuration: 150, // milliseconds for movement animation (reduced from 300)
        isAnimating: false, // Flag to prevent overlapping animations,
        selectedFile: null
    },

    created() {
        this.getCurBoardInfo();
    },

    methods: {
        async getCurBoardInfo() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await fetch("/fullUpdate");
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();

                this.board = data.maze.board;

                if (data.position && typeof data.position.x === 'number' && typeof data.position.y === 'number') {
                    this.heroPos = {
                        x: data.position.x,
                        y: data.position.y
                    };
                }

                this.boardCopy = this.board;
                console.log("Game data refreshed:", data);
                // auto solve
                await this.handleSolve();
            } catch (error) {
                console.error("Failed to refresh game data:", error);
                this.error = "Failed to load game data. Using default board.";
            } finally {
                this.isLoading = false;
            }
        },

        // Score and item collection methods
        getPositionKey(x, y) {
            return `${x}_${y}`;
        },

        calculateScoreChange(cellType) {
            switch (cellType) {
                case 'GOLD':
                    return 5;
                case 'TRAP':
                    return -3;
                case 'SKILL':
                    return 1; // Small bonus for collecting skills
                default:
                    return 0;
            }
        },

        addScoreChange(amount, reason) {
            this.score += amount;
            this.scoreHistory.push({
                amount: amount,
                reason: reason,
                timestamp: Date.now()
            });

            // Keep only last 10 score changes for performance
            if (this.scoreHistory.length > 10) {
                this.scoreHistory = this.scoreHistory.slice(-10);
            }
        },

        processItemCollection(x, y) {
            const posKey = this.getPositionKey(x, y);
            const cellType = this.board[x][y];

            // Skip if already collected
            if (this.collectedItems.has(posKey)) {
                return;
            }

            let scoreChange = 0;
            let newCellType = cellType;
            let reason = '';

            switch (cellType) {
                case 'GOLD':
                    scoreChange = 5;
                    newCellType = 'GOLD_COLLECTED';
                    reason = 'Gold collected';
                    this.goldCollected++;
                    break;
                case 'TRAP':
                    scoreChange = -3;
                    newCellType = 'TRAP_TRIGGERED';
                    reason = 'Trap triggered';
                    this.trapsTriggered++;
                    break;
                case 'SKILL':
                    scoreChange = 1;
                    newCellType = 'SKILL_COLLECTED';
                    reason = 'Skill collected';
                    this.skillsCollected++;
                    break;
                default:
                    return; // No processing needed for other cell types
            }

            // Mark as collected
            this.collectedItems.add(posKey);

            // Update the board with masked icon
            this.board[x][y] = newCellType;

            // Add score change
            if (scoreChange !== 0) {
                this.addScoreChange(scoreChange, reason);
            }
        },

        async updateBoardSize() {
            if (this.validSize < 5 || this.validSize > 99) {
                this.error = "Board size must be between 5 and 99";
                return;
            }

            try {
                // this will direct flush the full board
                // just notify the server to update the board size
                // the udpate of info need a more request
                const response = await fetch(`/game.html?size=${this.validSize}`, {
                    method: 'GET'
                });
                if (response.ok) {
                    this.flushData();
                    await this.getCurBoardInfo();
                } else {
                    this.error = "Failed to update board size";
                }
            } catch (error) {
                console.error("Failed to update board size:", error);
                this.error = "Failed to update board size";
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

        getCell(x, y) {
            const cellRef = this.$refs[`cell_${x}_${y}`];
            return cellRef && cellRef[0] ? cellRef[0] : null;
        },
        async handleSolveByGreedy() {
            console.log("Starting to solve the maze...");
            this.error = null;
            this.isLoading = true;
            try {
                const response = await fetch("/greedyPath");
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                this.path = data.path || [];
                this.segments = [];
                this.curInd = -1;
                console.log("Path data received:", this.path);
                this.processPathSegments();
                console.log("Path solved:", { path: this.path, segments: this.segments });
            } catch (error) {
                console.error("Failed to solve path:", error);
                this.error = "Failed to solve the maze. Please try again.";
            } finally {
                this.isLoading = false;
            }
        },
        async handleSolve() {
            console.log("Starting to solve the maze...");
            this.isLoading = true;
            this.error = null;
            try {
                const response = await fetch("/dpPath");
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                this.path = data.path || [];
                this.segments = [];
                this.curInd = -1;

                console.log("Path data received:", this.path);
                this.processPathSegments();

                console.log("Path solved:", { path: this.path, segments: this.segments });
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
                    (this.board[point.x][point.y] === "GOLD"
                        || this.board[point.x][point.y] === "BOSS"
                        || this.board[point.x][point.y] === "EXIT")) {
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

        flushData() {
            this.path = [];
            this.segments = [];
            this.steps = 0;
            this.score = 0;
            this.scoreHistory = [];
            this.collectedItems.clear();
            this.goldCollected = 0;
            this.trapsTriggered = 0;
            this.skillsCollected = 0;
            this.error = null;
            this.curInd = -1;
            this.stopAutoPlay(); // Stop auto-play when flushing
            this.clearOldStyle();
        },

        resetGame() {
            this.steps = 0;
            this.score = 0;
            this.scoreHistory = [];
            this.collectedItems.clear();
            this.goldCollected = 0;
            this.trapsTriggered = 0;
            this.skillsCollected = 0;
            this.error = null;
            this.curInd = -1;
            this.board;
            this.stopAutoPlay(); // Stop auto-play when resetting
            this.clearOldStyle();
            this.getCurBoardInfo();
        },

        clearOldStyle() {
            // Clear all cell highlights
            this.board.forEach((row, x) => {
                row.forEach((_, y) => {
                    const cell = this.getCell(x, y);
                    if (cell) {
                        cell.style.backgroundColor = '';
                        cell.classList.remove('hero-moving', 'path-highlight');
                    }
                });
            });
        },

        highlightSegment(segmentIndex) {
            if (segmentIndex < 0 || segmentIndex >= this.segments.length) {
                console.warn("Invalid segment index:", segmentIndex);
                return;
            }

            // Prevent multiple segments from playing simultaneously
            if (this.isAnimating) {
                console.warn("Animation already in progress, skipping...");
                return;
            }

            this.isAnimating = true;
            const randomColor = `hsl(${Math.random() * 360}, 100%, 75%)`;
            const segment = this.segments[segmentIndex];

            // Calculate total animation time for this segment (faster timing)
            const segmentAnimationTime = segment.length * (this.animationDuration / 3); // Reduced from /2

            // Animate through each point in the segment
            segment.forEach((point, pointIndex) => {
                setTimeout(() => {
                    this.stepCnt++;

                    // Process item collection BEFORE updating position
                    this.processItemCollection(point.x, point.y);

                    // Clear previous hero position animation
                    const prevCell = this.getCell(this.heroPos.x, this.heroPos.y);
                    if (prevCell) {
                        prevCell.classList.remove('hero-moving');
                    }

                    // Update hero position
                    this.heroPos = { x: point.x, y: point.y };

                    // Add movement animation to new position
                    const cell = this.getCell(point.x, point.y);
                    if (cell) {
                        cell.style.backgroundColor = randomColor;
                        cell.classList.add('hero-moving', 'path-highlight');

                        // Remove animation class after animation completes
                        setTimeout(() => {
                            cell.classList.remove('hero-moving');
                        }, this.animationDuration);
                    }
                }, pointIndex * (this.animationDuration / 3)); // Reduced from /2
            });

            // Reset animation flag after segment completes
            setTimeout(() => {
                this.isAnimating = false;
            }, segmentAnimationTime + this.animationDuration);
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

            // Prevent stepping if already animating
            if (this.isAnimating) {
                console.warn("Animation in progress, please wait...");
                return;
            }

            // Clear previous highlight
            if (this.curInd >= 0) {
                this.clearSegmentHighlight(this.curInd);
            }

            // Move to next segment
            if (this.curInd >= this.segments.length - 1) {
                if (this.isAutoPlaying) {
                    this.stopAutoPlay(); // Stop auto-play when reaching the end
                }
                return;
            }

            this.curInd++;
            this.highlightSegment(this.curInd);
            console.log(`Stepped to segment ${this.curInd + 1} of ${this.segments.length}`, this.segments[this.curInd]);
        },

        // Auto-play methods
        startAutoPlay() {
            if (this.segments.length === 0) {
                this.error = "No path segments available. Solve the maze first.";
                return;
            }

            if (this.isAnimating) {
                console.warn("Cannot start auto-play while animation is in progress");
                return;
            }

            this.isAutoPlaying = true;
            this.scheduleNextStep();
        },

        scheduleNextStep() {
            if (!this.isAutoPlaying) return;

            // Calculate delay: base speed + time for current segment animation
            const currentSegment = this.segments[this.curInd + 1];
            const segmentAnimationTime = currentSegment ?
                currentSegment.length * (this.animationDuration / 3) : 0; // Reduced from /2

            const totalDelay = this.autoPlaySpeed + segmentAnimationTime;

            this.autoPlayTimer = setTimeout(() => {
                if (!this.isAutoPlaying) return;

                if (this.curInd >= this.segments.length - 1) {
                    this.stopAutoPlay();
                    return;
                }

                this.stepOne();

                // Schedule next step if still auto-playing
                if (this.isAutoPlaying) {
                    this.scheduleNextStep();
                }
            }, totalDelay);
        },
        onFileSelected(event) {
            this.selectedFile = event.target.files[0];
            console.log("File selected:", this.selectedFile?.name);
        },

        async uploadMazeFile(useDefault = false) {
            // if use default is true, skip file validation
            if (!useDefault && !this.selectedFile) {
                this.error = "Please select a file to upload";
                return;
            }

            this.isLoading = true;
            this.error = null;

            try {
                const formData = new FormData();

                // Only append file if not using default
                if (!useDefault && this.selectedFile) {
                    formData.append('mazeFile', this.selectedFile);
                }

                const endpoint = useDefault ? '/loadMaze' : '/uploadMaze';
                const response = await fetch(endpoint, {
                    method: 'POST',
                    body: useDefault ? null : formData
                });

                if (!response.ok) {
                    throw new Error(`${useDefault ? 'Load' : 'Upload'} failed: ${response.status} ${response.statusText}`);
                }

                const result = await response.json();
                console.log(`Maze ${useDefault ? 'loaded' : 'uploaded'} successfully:`, result);


                this.flushData();
                await this.getCurBoardInfo();

            } catch (error) {
                console.error(`Failed to ${useDefault ? 'load default' : 'upload'} maze file:`, error);
                this.error = `Failed to ${useDefault ? 'load default' : 'upload'} maze file: ${error.message}`;
            } finally {
                this.isLoading = false;
            }
        },
        stopAutoPlay() {
            this.isAutoPlaying = false;
            if (this.autoPlayTimer) {
                clearTimeout(this.autoPlayTimer);
                this.autoPlayTimer = null;
            }
        },

        toggleAutoPlay() {
            if (this.isAutoPlaying) {
                this.stopAutoPlay();
            } else {
                this.startAutoPlay();
            }
        },

        setAutoPlaySpeed(speed) {
            this.autoPlaySpeed = parseInt(speed);
            // Don't restart auto-play automatically, let current timing complete
        },

        restartAutoPlay() {
            this.stopAutoPlay();

            // Wait for any current animation to finish before restarting
            setTimeout(() => {
                this.curInd = -1;
                this.stepCnt = 0;
                this.clearOldStyle();
                this.startAutoPlay();
            }, this.isAnimating ? this.animationDuration : 0);
        }
    },

    computed: {
        gameStatus() {
            if (this.isLoading) return "Loading...";
            if (this.error) return "Error";
            if (this.isAutoPlaying) return "Auto-playing...";
            return "Ready";
        },

        totalPathLength() {
            return this.path.length;
        },

        autoPlayProgress() {
            if (this.segments.length === 0) return 0;
            return Math.round(((this.curInd + 1) / this.segments.length) * 100);
        },

        totalItemsCollected() {
            return this.collectedItems.size;
        },

        recentScoreChanges() {
            // Return last 3 score changes for display
            return this.scoreHistory.slice(-3).reverse();
        },

        gameStats() {
            return {
                score: this.score,
                goldCollected: this.goldCollected,
                trapsTriggered: this.trapsTriggered,
                skillsCollected: this.skillsCollected,
                totalSteps: this.stepCnt
            };
        }
    },

    // Clean up timer when component is destroyed
    beforeDestroy() {
        this.stopAutoPlay();
    }
});
