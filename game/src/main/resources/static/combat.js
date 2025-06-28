let bossHP = [];
let playerSkills = [];
let actions = [];
let turn = 0;
let currentBoss = 0;

function initFromRawData(skillsArray) {
    return skillsArray.map(skill => ({
        damage: skill[0],
        cooldown: skill[1],
        currentCooldown: 0
    }));
}

function updateDisplay() {
    const skillDiv = document.getElementById("skill-status");
    skillDiv.innerHTML = "";
    playerSkills.forEach((s, i) => {
        const div = document.createElement("div");
        div.className = "skill";
        div.innerHTML = `技能 ${i}: 伤害 ${s.damage}, 冷却时间 ${s.cooldown}, <span class="cooldown">当前冷却: ${s.currentCooldown}</span>`;
        skillDiv.appendChild(div);
    });

    const bossDiv = document.getElementById("boss-status");
    bossDiv.innerHTML = "";
    bossHP.forEach((hp, i) => {
        const div = document.createElement("div");
        div.className = "boss";
        div.innerText = `Boss ${i + 1}: HP = ${hp}`;
        bossDiv.appendChild(div);
    });
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
    span.textContent = `→ 技能 ${skillIndex} `;
    seqDiv.appendChild(span);


    if (turn >= actions.length || currentBoss >= bossHP.length) {
        document.getElementById("next-turn").disabled = true;
        document.getElementById("exit").hidden = false;
    }
}

function exitCombat() {
    alert("战斗结束，关闭网页！");
    window.close(); // 注意：仅 JS 打开窗口才可关闭，浏览器限制
}

function startCombat() {
    fetch("http://localhost:8080/startCombat")
        .then(response => response.json())
        .then(data => {
            bossHP = data.Boss;
            actions = data.actions;
            playerSkills = initFromRawData(data.PlayerSkills);
            turn = 0;
            currentBoss = 0;

            document.getElementById("start").hidden = true;
            document.getElementById("next-turn").hidden = false;
            document.getElementById("exit").hidden = true;
            document.getElementById("skill-sequence").innerHTML = "";

            updateDisplay();
        })
        .catch(error => {
            console.error("战斗初始化失败:", error);
            alert("无法开始战斗，请检查服务器！");
        });
}
