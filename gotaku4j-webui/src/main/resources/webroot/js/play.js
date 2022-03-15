const websocket = new PlayWebSocket();
const NONE = 0;
const CORRECT = 1;
const WRONG = 2;

let stageQuizNumber = 0;
let totalQuizNumber = 0;
let totalScore = 0;
let stageNumber = 1;
let borders = [];
let quizStatus = [];
let stageCorrectAnswers = 0;
let totalCorrectAnswers = 0;

function resetAll() {
	stageQuizNumber = 0;
	totalQuizNumber = 0;
	totalScore = 0;
	stageNumber = 1;
	borders = [4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8];
	quizStatus = [];
	stageCorrectAnswers = 0;
	totalCorrectAnswers = 0;
	clearTimeout(countDownTimer);
	$("#span-stage-number").text(stageNumber);
	$(".btn-selection").prop("disabled", true);
}

$(function () {
const validKeys = ["1", "2", "3", "4", "5"];

  document.addEventListener('keydown', e=>{
    try {
      if(e.key=="ArrowUp"||e.key=="ArrowDown"){
        const f = $('.btn-selection:focus');
        const v = parseInt(f.attr("data-selection"));
        const n = e.key=="ArrowUp"? v-1:v+1;
        if(!n){
          window.setTimeout(() => document.getElementById("btn-s-0").focus(), 0);
        }else if(0<=n&&n<=4){
          window.setTimeout(() => document.getElementById("btn-s-"+n).focus(), 0);
        }
      }
    } catch (error) {
      window.setTimeout(() => document.getElementById("btn-s-0").focus(), 0);
    }

    if(validKeys.includes(e.key) && $("#btn-s-0").prop("disabled")){
      $("#btn-s-"+(parseInt(e.key)-1)).click();
    }
    return false;
  });

    document.getElementById("audio-ok").load();
    document.getElementById("audio-ng").load();
    document.getElementById("audio-next-stage").load();
    document.getElementById("audio-select-book").load();
    document.getElementById("audio-show-quiz").load();
    document.getElementById("audio-game-over").load();
    document.getElementById("audio-timer").volume=0.5;
    document.getElementById("audio-timer").load();
    document.getElementById("audio-border-line").load();


	$("#btn-start-game").click(function () {
		clearTimeout(countDownTimer);
		resetAll();
		websocket.sendAsJsonRpc("startGame", []);
	});

	$("#btn-group-book-titles").on('click', '.btn-book-title', function () {
		const title = $(this).text();
		$("#span-book-title").text(title);
		console.log(title);
		$("#div-start-ui").hide();
		websocket.sendAsJsonRpc("startBook", [title]);
		document.getElementById("audio-select-book").pause();
		websocket.sendAsJsonRpc("startStage", []);
		startStage();
	});

	$(".btn-selection").click(function () {
		const jadge = $(this).find(".selection").text() == answer;
		$(this).removeClass("btn-outline-dark active");
		$(this).addClass(jadge ? "btn-success" : "btn-danger");
		doJadge(jadge, $(this));
		if($(this)){
      websocket.sendAsJsonRpc("quizResult", [$(this).attr("data-selection"), $(this).text()]);
    }else{
      websocket.sendAsJsonRpc("quizResult", [null,null]);
    }

	});
	websocket.open();
});


function doJadge(jadge, selectedItem) {
	(jadge ?
		document.getElementById("audio-ok") : document.getElementById("audio-ng")).play();
	clearTimeout(countDownTimer);

	$(".btn-selection").prop("disabled", true);
	$("#div-jadge").removeClass();
	$("#div-prev-question").text("問題: " + $("#div-question").text());
	$("#div-prev-answer").text("正解: " + answer);
	$("#div-jadge").text(jadge ? "正解!" : "不正解!");
	$("#div-jadge").addClass(jadge ? "badge badge-success" : "badge badge-danger");

	quizStatus.push(jadge ? 1 : 2);

	if (jadge) {
		stageCorrectAnswers++;
		totalCorrectAnswers++;
		const time = $("#span-time").text() == "--" ? 10 : $("#span-time").text();
		totalScore += time * 100;
	}

	updateResultMeter();
	$("#span-total-score").text(totalScore);
	$("#span-total-ratio").text((totalCorrectAnswers * 100 / totalQuizNumber).toFixed(1));

	if (borders[stageNumber - 1] == 0) {
		if (jadge && stageQuizNumber < 8) {
			goNextQuiz();
		} else {
			finishStage();
		}
	} else {
		if (stageQuizNumber < 8) {
			goNextQuiz();
		} else {
			finishStage();
		}
	}
	function finishStage() {
		setTimeout(function () {
			websocket.sendAsJsonRpc("finishStage", [quizStatus]);
			if (stageCorrectAnswers >= borders[stageNumber - 1]) {
				document.getElementById("audio-next-stage").play();
				setTimeout(function () {
					if (stageNumber == 16) {
						finishGame();
					} else {
						stageNumber++;
						startStage();
					}
				}, 4000);
				swalAlert("ステージクリア", "次のステージに進みます", "success", function () {
				});
			} else {
				document.getElementById("audio-game-over").play();
				setTimeout(function () {
					finishGame();
				}, 4000);
				swalAlert("残念…", "ボーダーをクリアできませんでした", "error", function () {
				});
			}
		}, 4000);

		function finishGame() {
			swalInput("", "名前を入れてね", "", "名前", function (name) {
				websocket.sendAsJsonRpc("sendRecord", [name, totalQuizNumber, totalCorrectAnswers, totalScore]);
			});

		}
	}
}

function goNextQuiz() {
	setTimeout(function () {
		$(".btn-selection").addClass("btn-outline-dark");
		$(".btn-selection").removeClass("btn-success btn-danger active");
    $("#btn-s-3").focus();
		websocket.sendAsJsonRpc("nextQuiz", []);
	}, 3000);
	const ad = document.getElementById("audio-stage-1")
	ad.loop=true;
	ad.volume=0.3;
  ad.play();
}

function updateResultMeter() {
	let stat = [0, 0, 0];
	for (let i = 0; i < 8; i++) {
		if (quizStatus[i] == null) {
			stat[NONE] += 1;
		} else if (quizStatus[i] == 1) {
			stat[CORRECT] += 1;
		} else {
			stat[WRONG] += 1;
		}
	}
	let meter = "";
	for (let i = 0; i < stat[CORRECT]; i++) {
		meter += "<span class='text-primary'>■</span>";
	}
	for (let i = 0; i < stat[NONE]; i++) {
		meter += "■";
	}
	for (let i = 0; i < stat[WRONG]; i++) {
		meter += "<span class='text-danger'>■</span>";
	}

	$("#div-stage-score").html(meter);

}


function startStage() {
	stageQuizNumber = 0;
	stageCorrectAnswers = 0;
	quizStatus = [];

	$("#span-stage-number").text(stageNumber);
	updateResultMeter();
	$("#div-play-ui").show();
	$("#span-border-line-indicator").css({ "padding-left": 0 });
	document.getElementById("audio-border-line").play();

	const borderLine = borders[stageNumber - 1];
	$("#span-border-number").text(borderLine);

	$("#span-border-line-indicator").animate({
		"padding-left": (borderLine - 1) * 0.88 + "em",
	}, borderLine * 500, "linear", function () {
		document.getElementById("audio-border-line").pause();
		goNextQuiz();
	});
}

