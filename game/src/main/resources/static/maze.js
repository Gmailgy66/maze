
function handleNextStep() {
    fetch("http://localhost:8080/nextStep")
        .then(
            function(res){
                var score = document.getElementById('score')
                score.innerText = res.text.hero.score
                var x = res.text().position.x
                var y = res.text().position.y
                var cell = document.getElementById('table').rows[x].cells[y]
                cell.innerText = "Hero"
            }
        )
}