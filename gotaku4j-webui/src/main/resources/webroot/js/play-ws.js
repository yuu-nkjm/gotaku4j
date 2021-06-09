let answer = "";
let countDownTimer = null;

function startCountDown() {
  let time = 20;
  playCountDown();

  function playCountDown() {
    $("#span-time").text(time);
    if (time == 0) {
      document.getElementById("audio-ng").play();
      $(".btn-selection").prop("disabled", false);
      doJadge(false);
      return;
    }
    document.getElementById("audio-timer").currentTime = 0;
    document.getElementById("audio-timer").play();
    time--;
    clearTimeout(countDownTimer);
    countDownTimer = setTimeout(playCountDown, 1000);
  }
}


class PlayWebSocket {

  constructor() {
    this.connection = null;
    this.sessionId = null;
  }

  getSessionId() {
    return this.sessionId;
  }

  getConnection() {
    return this.connection;
  }

  sendAsJsonRpc(_method, _parameters) {
    this.connection.send(JSON.stringify({ method: _method, parameters: _parameters }));
  }

  open() {
    const self = this;
    let initialized = false;
    let connection = createConnection("userId", "gameId");
    this.connection = connection;

    $(window).on('unload', function () {
      if (connection) {
        connection.onclose = function () { }
        connection.close();
      }
    });

    connection.onmessage = function (e) {
      const json = JSON.parse(e.data);
      switch (json.method) {
        case "BOOK_TITLES":
          _bookTitles(json.parameters[0]);
          break;
        case "START_STAGE":
          break;
        case "QUIZ":
          _quiz(json.parameters[0]);
          break;
        default:
          console.error("invalid method name =>" + json.method);
      }
      function _bookTitles(titles) {
        document.getElementById("audio-select-book").currentTime = 0;
        document.getElementById("audio-select-book").play();
        $("#div-play-ui").hide();
        $("#div-start-ui").show();
        $("#btn-group-book-titles").empty();
        titles.forEach(title => {
          $("#btn-group-book-titles").append('<button class="btn btn-secondary btn-book-title">' + title + '</button> ');
        });

      }
      function _quiz(quiz) {
        document.getElementById("audio-show-quiz").play();
        stageQuizNumber++;
        totalQuizNumber++;
        $("#span-stage-quiz-number").text(stageQuizNumber);
        $("#div-question").html(quiz.question);
        for (var i = 0; i < 5; i++) {
          $("#btn-s-" + i).html("<span class='mr-2 small'>[" + (i + 1) + "]</span> " + "<span class='selection'>" + quiz.selections[i] + "</span>");
        }
        answer = quiz.answer;
        $("#span-time").text("--");

        setTimeout(function () {
          startCountDown();
          $(".btn-selection").prop("disabled", false);
        }, 2000);
      }

    };

    connection.onopen = function (e) {
      console.log("connection is open.");
      // console.log(stringifyEvent(e));
    };

    connection.onerror = function (e) {
      console.error("connection has an error.");
      console.error(stringifyEvent(e));
    };


    connection.onclose = function (e) {
      let reconTimer;
      console.warn("connection is closed.");
      clearTimeout(reconTimer);
      reconTimer = setTimeout(function () {
        self.open();
      }, 1000);
      console.warn(stringifyEvent(e));
    };

    function createConnection(userId, gameId) {
      const wsUrl = getWebSocketBaseUrl();
      return new WebSocket(wsUrl + "?userId=" + userId + "&gameId=" + gameId);

      function getWebSocketBaseUrl() {
        function createWebSocketUrl(protocol) {
          const u = parseUri(document.URL);
          const urlPrefix = protocol + "://" + u.authority + "/";
          return urlPrefix + "websocket/play";
        }
        return parseUri(location).protocol === "https" ? createWebSocketUrl("wss") : createWebSocketUrl("ws");
      }

    }
  }
}


