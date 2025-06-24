

function handleNextTurn(){
    fetch("http://localhost:8080/nextTurn").then(
        function(res){
            var text = document.getElementById("text");
            text.innerText = res.text().fight;
        }
    );
}