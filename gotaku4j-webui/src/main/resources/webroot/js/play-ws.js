let results;
class PlayWebSocket {

  constructor() {
    this.connection = null;
    this.correctAnswer = "";
    this.explanation = "";
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
        case "RELOAD":
          document.location.reload();
          break;
        case "GENRES":
          _showGenres(json.parameters[0]);
          break;
        case "RESULTS":
          _showResults(json.parameters[0]);
          break;
        case "RANKING":
          _rankings(json.parameters[0], json.parameters[1]);
          break;
        case "QUIZ":
          _doQuiz(json.parameters[0]);
          break;
        case "GAME_CLEAR":
          swalAlert("全ての問題を解きました", "おめでとう！", "success", function () {
            sendRecord();
          }, null, false, 4000);
          break;
        default:
          console.error("invalid method name =>" + json.method);
      }
      function _showGenres(_genres) {
        $("#div-ganres").empty();
        for (let i = 0; i < _genres.length; i++) {
          const _div = $('<div class="form-check"></div>');
          _div.append($('<input class="form-check-input ganre" type="checkbox" id="genre-' + i + '" checked>').val(_genres[i].GENRE));
          _div.append('<label class="form-check-label" for="ganre-' + i + '">' + _genres[i].GENRE + " (" + _genres[i].NUM + "題)" + '</label>')
          $("#div-ganres").append(_div);
        }
      }
      function _showResults(_results) {
        results = _results

        $('#div-tbl-results').empty();
        for (let i = 0; i < results.length; i++) {
          const _id = "tbl-results-" + i;
          const _tbl = $('<table class="table table-bordered table-striped table-hover small">').attr("id", _id);
          _tbl.append($('<thead><tr><th>問題集</th><th>QID</th><th>問題</th><th>正解</th><th>スコア</th><th>補足</th></tr></thead>'));
          $('#div-tbl-results').append($('<h4>').addClass("text-center text-primary").text(results[i].genre));
          $('#div-tbl-results').append(_tbl);
          _tbl.ready(function () {
            _tbl.DataTable({
              data: results[i].data,
              columns: [
                { data: 'BOOKNAME' },
                { data: 'QID' },
                { data: 'QUESTION' },
                { data: 'CHOICE1' },
                { data: 'SCORE' },
                { data: 'EXPLANATION' }
              ],
              pageLength: 1000
            });
          });
        }
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
        for (let i = 0; i < 5; i++) {
          $("#btn-s-" + i).html("<span class='mr-2 small'>[" + (i + 1) + "] </span> " + "<span class='selection h4'>" + _quiz.selections[i] + "</span>");
        }
        websocket.correctAnswer = _quiz.answer;
        websocket.explanation = _quiz.explanation;
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


