

function handleNextTurn(){
    fetch("http://localhost:8080/nextTurn").then(
        function(res){
            res.json().then(data =>{
                var text = document.getElementById("text");
                text.innerText = data.fight
            })
        }
    );
}