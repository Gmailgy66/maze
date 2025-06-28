// import {defineComponent, ref} from './vue'
// import "./question.css"

// const question = defineComponent({
//     name: 'question',
//     setup() {
//         const visible =defineModel()
//         const question = ref('')
//         const answer = ref(null)
//         const finish = ref(false)
//         fetch("http://localhost:8080/question").then(
//             res =>{
//                 res.json().then(data =>{
//                     question.value = data.question
//                     answer.value = data.ans
//                 })
//             }
//         )
//         function startSolve(){
//             finish.value = true
//             document.getElementById('ipt1').value = answer.value[0]
//             document.getElementById('ipt2').value = answer.value[1]
//             document.getElementById('ipt3').value = answer.value[2]
//         }
//
//         function endGame(){
//             visible.value = false
//         }
//
//         return {
//             visible,
//             question,
//             answer,
//             finish,
//             startSolve,
//             endGame
//         }
//     },
//     template: `
//         <div v-if="visible" class="quiz-box">
//             <p class="question">{{question}}</p>
//             <div class="input-container">
//                 <input type="number" min="0" max="9" maxlength="1" class="digit-box" id="ipt1">
//                 <input type="number" min="0" max="9" maxlength="1" class="digit-box" id="ipt2">
//                 <input type="number" min="0" max="9" maxlength="1" class="digit-box" id="ipt3">
//             </div>
//             <button @click="startSolve">开始解答</button>
//             <button v-if="finish" @click="endGame">结束</button>
//         </div>
//     `
// })

window.question = {
    name: 'question',
    data() {
        return {
            question: '',
            answer: null,
        }
    },
    mounted() {
        // 获取题目信息
        fetch("http://localhost:8080/quiz")
            .then(res => res.json())
            .then(data => {
                this.question = data.question
                this.answer = data.ans
            })
    },
    methods: {
        startSolve(){
            document.getElementById('question').innerText = this.question
            document.getElementById('output').innerText = this.answer
        },
        endGame() {
            this.$emit('close')
        }
    },
    template: `
    <div class="quiz-box">
      <p id="question"></p>
      <div class="result">
        <p id="output"></p>
      </div>
      <button @click="startSolve">开始解答</button>
      <button v-if="finish" @click="endGame">结束</button>
    </div>
  `
}
