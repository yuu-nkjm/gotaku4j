const websocket = new PlayWebSocket();

const STAGE_QUIZ_NUM = 8;
const TIME_LIMIT = 30;
const BOARDERS = [4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8];

const gameState = {
	stageQuizNumber: 0,
	totalQuizNumber: 0,
	totalScore: 0,
	stageNumber: 1,
	quizStatus: [],
	stageCorrectAnswers: 0,
	totalCorrectAnswers: 0
}

$(function () {

	const _validKeys = ["1", "2", "3", "4", "5"];

	document.addEventListener('keydown', e => {
		try {
			if (e.key == "ArrowUp" || e.key == "ArrowDown") {
				const f = $('.btn-selection:focus');
				const v = parseInt(f.attr("data-selection"));
				const n = e.key == "ArrowUp" ? v - 1 : v + 1;
				if (!n) {
					window.setTimeout(() => document.getElementById("btn-s-0").focus(), 0);
				} else if (0 <= n && n <= 4) {
					window.setTimeout(() => document.getElementById("btn-s-" + n).focus(), 0);
				}
			}
		} catch (error) {
			window.setTimeout(() => document.getElementById("btn-s-0").focus(), 0);
		}

		if (_validKeys.includes(e.key) && !$("#btn-s-0").prop("disabled")) {
			$("#btn-s-" + (parseInt(e.key) - 1)).click();
		}
		return false;
	});

	$("#div-players").on('click', '.btn-player', function () {
		const _player = $(this).text();
		$("#span-player").text(_player);
		websocket.sendAsJsonRpc("SELECT_PLAYER", [_player]);
		$("#div-player-select").hide();
		$("#div-book-select").show();
	});


	$("#btn-group-book-titles").on('click', '.label-book-title', function () {
		const _title = $(this).text();
		$("#span-book-title").text(_title);
		websocket.sendAsJsonRpc("SELECT_BOOK", [_title]);
		$("#btn-start-game").prop("disabled", false);
	});

	$("#btn-start-game").click(function () {
		window.addEventListener('beforeunload', function (e) {
			e.preventDefault();
			e.returnValue = 'Leave?';
			return "Leave?"
		});	
		$("#div-book-select").hide();
		websocket.sendAsJsonRpc("START_GAME", []);
		websocket.sendAsJsonRpc("START_STAGE", [gameState.stageNumber]);
		startStage();
	});

	$(".btn-selection").click(function () {
		const _jadge = $(this).find(".selection").text() == websocket.prevCorrectAnswer;
		$(this).removeClass("btn-outline-dark active");
		$(this).addClass(_jadge ? "btn-success" : "btn-danger");
		doJadge(_jadge, $(this));
	});

	$("#div-player-select").show();
	$("#div-book-select").hide();
	$("#div-play-ui").hide();
	$("#span-player").empty();
	$("#span-stage-number").text(gameState.stageNumber);
	$(".btn-selection").prop("disabled", true);

	$(".audio").each((i, e) => e.load());
	document.getElementById("audio-timer").volume = 0.5;
	document.getElementById("audio-select-book").volum = 0.5;
	document.getElementById("audio-select-book").play();
	document.getElementById("audio-stage-1").loop = true;
	document.getElementById("audio-stage-1").volume = 0.3;

	websocket.open();
});


function doJadge(_jadge, _selectedItem) {
	(_jadge ?
		document.getElementById("audio-ok") : document.getElementById("audio-ng")).play();
	clearTimeout(websocket.countDownTimer);

	$(".btn-selection").prop("disabled", true);
	$("#div-jadge").removeClass();
	$("#div-prev-question").text($("#div-question").text());
	$("#div-prev-answer").text(websocket.prevCorrectAnswer);
	const prevChoice = !_selectedItem ? "" : _selectedItem.text().split("]: ")[1];
	$("#div-prev-selection").text(prevChoice);
	$("#div-jadge").text(_jadge ? "○" : "×");
	$("#div-jadge").addClass("badge " + (_jadge ? "bg-success" : "bg-danger"));

	gameState.quizStatus.push(_jadge ? 1 : 2);

	const restTime = $("#span-time").text() == "--" ? TIME_LIMIT : $("#span-time").text();

	if (_jadge) {
		gameState.stageCorrectAnswers++;
		gameState.totalCorrectAnswers++;
		gameState.totalScore += restTime * 100;
	}

	websocket.sendAsJsonRpc("QUIZ_RESPONSE", [TIME_LIMIT - restTime, prevChoice, _jadge]);


	updateResultMeter();
	$("#span-total-score").text(gameState.totalScore);
	$("#span-total-ratio").text((gameState.totalCorrectAnswers * 100 / gameState.totalQuizNumber).toFixed(1));

	if (BOARDERS[gameState.stageNumber - 1] == 0) {
		if (_jadge && gameState.stageQuizNumber < STAGE_QUIZ_NUM) {
			goNextQuiz();
		} else {
			finishStage();
		}
	} else {
		if (gameState.stageQuizNumber < STAGE_QUIZ_NUM) {
			goNextQuiz();
		} else {
			finishStage();
		}
	}
	function finishStage() {
		setTimeout(function () {
			websocket.sendAsJsonRpc("FINISH_STAGE", [gameState.quizStatus]);
			if (gameState.stageCorrectAnswers >= BOARDERS[gameState.stageNumber - 1]) {
				document.getElementById("audio-next-stage").play();

				if (gameState.stageNumber == BOARDERS.length - 1) {
					swalAlert("最終ステージクリア", "おめでとう！", "success", function () {
						sendRecord();
					}, null, false, 4000);
				} else {
					swalAlert("ステージクリア", "次のステージに進みます", "success", function () {
						gameState.stageNumber++;
						startStage();
					}, null, false, 4000);
				}
			} else {
				document.getElementById("audio-game-over").play();
				swalAlert("残念…", "ボーダーをクリアできませんでした", "error", function () {
					sendRecord();
				}, null, false, 4000);
			}
		}, 4000);

		function sendRecord() {
			websocket.sendAsJsonRpc("SEND_RECORD", [gameState.totalQuizNumber, gameState.totalCorrectAnswers, gameState.totalScore]);
		}
	}
}


function goNextQuiz() {
	setTimeout(function () {
		$(".btn-selection").addClass("btn-outline-dark");
		$(".btn-selection").removeClass("btn-success btn-danger active");
		websocket.sendAsJsonRpc("NEXT_QUIZ", [gameState.stageQuizNumber + 1]);
	}, 3000);
}

function updateResultMeter() {
	const _NONE = 0;
	const _CORRECT = 1;
	const _WRONG = 2;

	let _stat = [0, 0, 0];
	let _quizStatus = gameState.quizStatus;
	for (let i = 0; i < 8; i++) {
		if (_quizStatus[i] == null) {
			_stat[_NONE] += 1;
		} else if (_quizStatus[i] == 1) {
			_stat[_CORRECT] += 1;
		} else {
			_stat[_WRONG] += 1;
		}
	}
	let _meter = "";
	for (let i = 0; i < _stat[_CORRECT]; i++) {
		_meter += "<span class='text-primary'>■</span>";
	}
	for (let i = 0; i < _stat[_NONE]; i++) {
		_meter += "■";
	}
	for (let i = 0; i < _stat[_WRONG]; i++) {
		_meter += "<span class='text-danger'>■</span>";
	}

	$("#div-stage-score").html(_meter);

}


function startStage() {
	$(".audio").each((i, e) => e.pause());
	gameState.stageQuizNumber = 0;
	gameState.stageCorrectAnswers = 0;
	gameState.quizStatus = [];

	websocket.sendAsJsonRpc("START_STAGE", [gameState.stageNumber]);

	$("#span-stage-number").text(gameState.stageNumber);
	updateResultMeter();
	$("#div-play-ui").show();
	$("#span-border-line-indicator").css({ "padding-left": 0 });
	document.getElementById("audio-border-line").play();

	const _borderLine = BOARDERS[gameState.stageNumber - 1];
	$("#span-border-number").text(_borderLine);

	$("#span-border-line-indicator").animate({
		"padding-left": (_borderLine - 1) * 0.88 + "em",
	}, _borderLine * 500, "linear", function () {
		goNextQuiz();
		document.getElementById("audio-stage-1").play();
	});
}

