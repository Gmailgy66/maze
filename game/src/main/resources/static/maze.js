
function handleNextStep() {
    fetch("http://localhost:8080/nextStep")
        .then(
            function(res){
                res.json().then(data=>{
                    var score = document.getElementById('score')
                    var x = res.text().position.x
                    var y = res.text().position.y
                    var cell = document.getElementById('table').rows[x].cells[y]
                    score.innerText = data.hero.score
                    cell.innerText = "Hero"
                })

            }
        )
}