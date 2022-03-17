
class PlayWebSocket {

  constructor() {
    this.connection = null;
    this.prevCorrectAnswer = "";
    this.countDownTimer = null;
  }

  sendAsJsonRpc(_method, _parameters) {
    this.connection.send(JSON.stringify({ method: _method, parameters: _parameters }));
  }

  open() {
    const self = this;
    let initialized = false;
    let connection = createConnection();
    this.connection = connection;

    $(window).on('unload', function () {
      if (connection) {
        connection.onclose = function () { }
        connection.close();
      }
    });

    function createConnection() {
      const wsUrl = getWebSocketBaseUrl();
      return new WebSocket(wsUrl);

      function getWebSocketBaseUrl() {
        function createWebSocketUrl(protocol) {
          const u = parseUri(document.URL);
          const urlPrefix = protocol + "://" + u.authority + "/";
          return urlPrefix + "websocket/play";
        }
        return parseUri(location).protocol === "https" ? createWebSocketUrl("wss") : createWebSocketUrl("ws");
      }

    }

    connection.onopen = function (e) {
      console.log("connection is open.");
      // console.log(stringifyEvent(e));
    };

    connection.onerror = function (e) {
      document.location.reload();
    };


    connection.onclose = function (e) {
      let _reconTimer;
      console.warn("connection is closed.");
      clearTimeout(_reconTimer);
      _reconTimer = setTimeout(function () {
        self.open();
      }, 1000);
      console.warn(stringifyEvent(e));
    };

    connection.onmessage = function (e) {
      const json = JSON.parse(e.data);
      switch (json.method) {
        case "RANKING":
          _rankings(json.parameters[0], json.parameters[1]);
          break;
        case "QUIZ":
          _doQuiz(json.parameters[0]);
          break;
        default:
          console.error("invalid method name =>" + json.method);
      }

      function _rankings(_scoreRankings, _rateRankings) {
        _ranking("#tbody-score-ranking", _scoreRankings);
        _ranking("#tbody-rate-ranking", _rateRankings);
      }

      function _ranking(_selector, _rankings) {
        $(_selector).empty();
        _rankings.forEach(_ranking => {
          const tr = $('<tr>');
          for (let index = 0; index < 6; index++) {
            tr.append($('<td>').text(_ranking[index]));
          }
          $(_selector).append(tr);
        });
      }
      function _doQuiz(_quiz) {
        document.getElementById("audio-show-quiz").play();
        gameState.stageQuizNumber++;
        gameState.totalQuizNumber++;
        $("#span-stage-quiz-number").text(gameState.stageQuizNumber);
        $("#div-question").html(_quiz.question);
        for (var i = 0; i < 5; i++) {
          $("#btn-s-" + i).html("<span class='mr-2 small'>[" + (i + 1) + "]: </span> " + "<span class='selection h4'>" + _quiz.selections[i] + "</span>");
        }
        websocket.prevCorrectAnswer = _quiz.answer;
        $("#span-time").text("--");

        setTimeout(function () {
          startCountDown();
          $(".btn-selection").prop("disabled", false);
          $("#btn-s-0").focus();

          function startCountDown() {

            let _time = TIME_LIMIT;
            playCountDown();

            function playCountDown() {
              $("#span-time").text(_time);
              if (_time == 0) {
                document.getElementById("audio-ng").play();
                $(".btn-selection").prop("disabled", false);
                doJadge(false);
                return;
              }
              if (_time <= 10) {
                const _ad = document.getElementById("audio-timer");
                _ad.currentTime = 0;
                _ad.play();
              }
              _time--;
              clearTimeout(websocket.countDownTimer);
              websocket.countDownTimer = setTimeout(playCountDown, 1200);
            }
          }
        }, 2000);
      }
    };
  }
}


