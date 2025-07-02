let bossHP = [];
let playerSkills = [];
let actions = [];
let turn = 0;
let currentBoss = 0;
let selectedFile = null;
let isAutoPlaying = false;
let autoPlayTimer = null;
let autoPlaySpeed = 1500; // Default speed in milliseconds

function initFromRawData(skillsArray) {
    return skillsArray.map(skill => ({
        damage: skill[0],
        cooldown: skill[1],
        currentCooldown: 0
    }));
}

function onBossFileSelected(event) {
    selectedFile = event.target.files[0];
    const fileNameSpan = document.getElementById("file-name");
    const uploadBtn = document.getElementById("upload-btn");

    if (selectedFile) {
        fileNameSpan.textContent = selectedFile.name;
        uploadBtn.disabled = false;
    } else {
        fileNameSpan.textContent = "";
        uploadBtn.disabled = true;
    }
}

async function uploadBossFile() {
    if (!selectedFile) {
        alert("请选择一个文件");
        return;
    }

    const formData = new FormData();
    formData.append('bossFile', selectedFile);

    try {
        const response = await fetch('/uploadBossConfig', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const result = await response.json();
            alert("Boss配置上传成功！");
            console.log("Upload result:", result);
            clearCombatData(); // Clear old data after successful upload
        } else {
            alert("上传失败，请检查文件格式");
        }
    } catch (error) {
        console.error("Upload error:", error);
        alert("上传失败：" + error.message);
    }
}

async function useDefaultBoss() {
    try {
        const response = await fetch('/loadDefaultBoss', {
            method: 'POST'
        });

        if (response.ok) {
            const result = await response.json();
            // alert("默认Boss配置加载成功！");
            console.log("Default boss loaded:", result);
            clearCombatData(); // Clear old data after successful load
        } else {
            alert("加载默认配置失败");
        }
    } catch (error) {
        console.error("Load default error:", error);
        alert("加载失败：" + error.message);
    }
}

function clearCombatData() {
    // Reset all combat state variables
    bossHP = [];
    playerSkills = [];
    actions = [];
    turn = 0;
    currentBoss = 0;

    // Stop auto-play if running
    stopAutoPlay();

    // Reset UI to initial state
    document.getElementById("start").hidden = false;
    document.getElementById("next-turn").hidden = true;
    document.getElementById("auto-play").hidden = true;
    document.getElementById("auto-controls").hidden = true;
    document.getElementById("exit").hidden = true;
    document.getElementById("combat-stats").hidden = true;
    document.getElementById("restart").hidden = true;

    // Clear displays
    document.getElementById("skill-sequence").innerHTML = "";
    document.getElementById("skill-status").innerHTML = "";
    document.getElementById("boss-status").innerHTML = "";

    console.log("Combat data cleared");
}

function restartCombat() {
    clearCombatData();
    startCombat();
    // no comfirm needed
    // if (confirm("确定要重新开始战斗吗？当前进度将丢失。")) {
    // }
}

function updateDisplay() {
    updateSkillDisplay();
    updateBossDisplay();
    updateStats();
}

function updateSkillDisplay() {
    const skillDiv = document.getElementById("skill-status");
    skillDiv.innerHTML = "";
    playerSkills.forEach((s, i) => {
        const div = document.createElement("div");
        div.className = `skill ${s.currentCooldown > 0 ? 'on-cooldown' : ''}`;
        div.innerHTML = `
            <div>⚔️ 技能 ${i}</div>
            <div>💥 伤害: ${s.damage}</div>
            <div>⏱️ 冷却: ${s.cooldown} 回合</div>
            <div class="cooldown">🕒 当前冷却: ${s.currentCooldown}</div>
        `;
        skillDiv.appendChild(div);
    });
}

function updateBossDisplay() {
    const bossDiv = document.getElementById("boss-status");
    bossDiv.innerHTML = "";
    bossHP.forEach((hp, i) => {
        const div = document.createElement("div");
        div.className = `boss ${hp <= 0 ? 'defeated' : ''} ${i === currentBoss && hp > 0 ? 'current-target' : ''}`;
        div.innerHTML = `
            🦖 Boss ${i + 1}<br>
            ❤️ HP: ${hp}
            ${i === currentBoss && hp > 0 ? '<br>🎯 当前目标' : ''}
            ${hp <= 0 ? '<br>💀 已击败' : ''}
        `;
        bossDiv.appendChild(div);
    });
}

function updateStats() {
    document.getElementById("current-turn").textContent = turn;
    document.getElementById("defeated-bosses").textContent = bossHP.filter(hp => hp <= 0).length;
    document.getElementById("remaining-bosses").textContent = bossHP.filter(hp => hp > 0).length;
}

function setAutoPlaySpeed(speed) {
    autoPlaySpeed = parseInt(speed);

    // If auto-play is currently running, restart it with new speed
    if (isAutoPlaying) {
        stopAutoPlay();
        startAutoPlay();
    }
}

function getSpeedLabel(speed) {
    if (speed <= 300) return "极快";
    if (speed <= 700) return "快速";
    if (speed <= 1500) return "正常";
    if (speed <= 2500) return "慢速";
    return "极慢";
}

function updateSpeedDisplay() {
    const speedLabel = document.getElementById("speed-label");
    if (speedLabel) {
        speedLabel.textContent = `速度: ${getSpeedLabel(autoPlaySpeed)} (${autoPlaySpeed}ms)`;
    }
}

function reduceCooldowns() {
    for (let skill of playerSkills) {
        if (skill.currentCooldown > 0) skill.currentCooldown--;
    }
}

function handleNextTurn() {
    if (turn >= actions.length) return;

    const skillIndex = actions[turn];
    const skill = playerSkills[skillIndex];

    if (skill.currentCooldown > 0) {
        alert(`技能 ${skillIndex} 正在冷却中（发生异常）`);
        return;
    }

    // Store the target boss before applying damage
    const targetBoss = currentBoss;

    skill.currentCooldown = skill.cooldown;
    bossHP[currentBoss] -= skill.damage;
    if (bossHP[currentBoss] < 0) bossHP[currentBoss] = 0;

    while (currentBoss < bossHP.length && bossHP[currentBoss] === 0) {
        currentBoss++;
    }

    turn++;
    reduceCooldowns();
    updateDisplay();

    const seqDiv = document.getElementById("skill-sequence");
    const span = document.createElement("span");
    span.className = "action-item";
    span.innerHTML = `
        <span class="skill-info">技能 ${skillIndex}</span>
        <span class="target-info">→ Boss ${targetBoss + 1}</span>
        <span class="damage-info">-${skill.damage}</span>
    `;
    seqDiv.appendChild(span);

    if (turn >= actions.length || currentBoss >= bossHP.length) {
        document.getElementById("next-turn").disabled = true;
        document.getElementById("auto-play").hidden = true;
        document.getElementById("exit").hidden = false;
        stopAutoPlay();

        if (currentBoss >= bossHP.length) {
            alert("🎉 恭喜！所有Boss都被击败了！");
        } else {
            alert("⚠️ 技能序列结束，但仍有Boss存活");
        }
    }
}

function toggleAutoPlay() {
    if (isAutoPlaying) {
        stopAutoPlay();
    } else {
        startAutoPlay();
    }
}

function startAutoPlay() {
    isAutoPlaying = true;
    document.getElementById("auto-play").textContent = "⏸️ 暂停自动";
    document.getElementById("next-turn").disabled = true;

    autoPlayTimer = setInterval(() => {
        if (turn >= actions.length || currentBoss >= bossHP.length) {
            stopAutoPlay();
            return;
        }
        handleNextTurn();
    }, autoPlaySpeed);
}

function stopAutoPlay() {
    isAutoPlaying = false;
    document.getElementById("auto-play").textContent = "▶️ 自动播放";
    document.getElementById("next-turn").disabled = false;

    if (autoPlayTimer) {
        clearInterval(autoPlayTimer);
        autoPlayTimer = null;
    }
}

function exitCombat() {
    if (confirm("确定要退出战斗吗？")) {
        window.location.href = "/game.html";
    }
}

function startCombat() {
    // if (!selectedFile) {
    //     alert("请选择一个文件");
    //     return;
    // }

    const formData = new FormData();
    formData.append('bossFile', selectedFile);
    fetch('startCombat',{
    // fetch('/uploadBossConfig', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            bossHP = [...data.Boss];
            actions = [...data.actions];
            playerSkills = initFromRawData(data.PlayerSkills);
            turn = 0;
            currentBoss = 0;

            document.getElementById("start").hidden = true;
            document.getElementById("next-turn").hidden = false;
            document.getElementById("auto-play").hidden = false;
            document.getElementById("auto-controls").hidden = false;
            document.getElementById("restart").hidden = false;
            document.getElementById("exit").hidden = true;
            document.getElementById("combat-stats").hidden = false;
            document.getElementById("skill-sequence").innerHTML = "";

            updateDisplay();
            updateSpeedDisplay();
        })
        .catch(error => {
            console.error("战斗初始化失败:", error);
            alert("无法开始战斗，请检查服务器！");
        });
}
