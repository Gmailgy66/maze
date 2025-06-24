
function handleNextStep() {
    fetch("http://localhost:8080/nextStep")
        .then(
            function(res){
                res.json().then(data=>{
                    var score = document.getElementById('score')
                    var x = data["position"].x
                    var y = data["position"].y
                    var cell = document.getElementById('table').rows[x].cells[y]
                    score.innerText = data.hero.score
                    cell.innerText = "Hero"
                })

            }
        )
}