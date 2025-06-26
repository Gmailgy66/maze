var hasStarted = false;

function startCombat(){
    // fetch("http://localhost:8080/combat").then(
    //     function(res){
    //         res.json().then(data =>{
    //             alert(data)
    //             var text = document.getElementById("text");
    //             text.innerText = data.fight
    //         })
    //     }
    // );
    var text = document.getElementById("text");
    text.innerText = 'open combat!'
    document.getElementById('next-turn').hidden=false
    document.getElementById('start-combat').hidden=true
}

function handleNextTurn(){
    fetch("http://localhost:8080/nextTurn").then(
        function(res){
            res.json().then(data =>{
                var text = document.getElementById("text");
                text.innerText += data.fight
                if(text === "You win!"){
                    document.getElementById('next-turn').hidden=true
                    document.getElementById('exit-combat').hidden=false
                }
            })
        }
    );
}

function exitCombat(){
    window.close()
}