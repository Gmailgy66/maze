import {defineComponent, ref} from 'vue'
import "./question.css"

const question = defineComponent({
    name: 'question',
    setup() {
        const visible = ref(false)
        const question = ref('')
        const answer = ref(null)
        const finish = ref(false)
        fetch("http://localhost:8080/question").then(
            res =>{
                res.json().then(data =>{
                    question.value = data.question
                    answer.value = data.ans
                })
            }
        )
        function startSolve(){
            finish.value = true
            document.getElementById('ipt1').value = answer.value[0]
            document.getElementById('ipt2').value = answer.value[1]
            document.getElementById('ipt3').value = answer.value[2]
        }
        return {
            visible,
            question,
            answer,
            finish,
            startSolve
        }
    },
    template: `
        <div v-if="visible" class="quiz-box">
            <p class="question">{{question}}</p>
            <div class="input-container">
                <input type="number" min="0" max="9" maxlength="1" class="digit-box" id="ipt1">
                <input type="number" min="0" max="9" maxlength="1" class="digit-box" id="ipt2">
                <input type="number" min="0" max="9" maxlength="1" class="digit-box" id="ipt3">
            </div>
            <button @click="startSolve">开始解答</button>
            <button v-if="finish">结束</button>
        </div>
    `
})