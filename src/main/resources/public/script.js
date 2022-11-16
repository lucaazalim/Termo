var socket;
var match;
var alertTimeout;

const inProgressMatchState = "IN_PROGRESS";
const outOfGuessesMatchState = "OUT_OF_GUESSES"
const guessedMatchState = "GUESSED";

open();

function open() {

    console.log("Opening connection...");
    socket = new WebSocket("ws://localhost:80/socket");

    socket.onopen = function(event) {

        console.log("Connected!");

    };

    socket.onclose = function(event) {

        console.log("Disconnected! -> " + event.code + " -> " + event.reason);

    };

    socket.onerror = function(event) {
        console.error("WebSocket error observed:", event);
    };

    socket.onmessage = function(event) {

        var json = JSON.parse(event.data);
        console.log("Received data: " + JSON.stringify(json));

        if (json.type == "handshake") {

            var matchSettings = new MatchSettings(5, 6);
            match = new Match(json.uuid, matchSettings, null);
            document.getElementById("input_guess").setAttribute("maxlength", match.settings.wordLength);
            new Packet("gameSettings", matchSettings).send();

        } else if(json.type == "word_grid") {

            match.wordGrid = json.content;
            renderWordGrid();

            if(match.wordGrid.state === guessedMatchState) {
                alert("<strong>Parabéns!</strong> Você adivinhou a palavra!", "success", false);
            } else if(match.wordGrid.state === outOfGuessesMatchState) {
                alert("Você gastou todas as " + match.settings.maxGuesses + " tentativas. :(", "danger", false);
            }

        } else if("alert") {

            alert(json.content.message, json.content.type.toLowerCase());

        }

    }

}

function restart() {
    new Packet("restart", null).send();
    closeAlert();
    clearWordInput();
}

function renderWordGrid() {

    var words = match.wordGrid.words;
    var html = "<table>";

    for(lineIndex = 0; lineIndex < match.settings.maxGuesses; lineIndex++) {

        html += "<tr>";
        var empty = lineIndex >= words.length;

        for(colIndex = 0; colIndex < match.settings.wordLength; colIndex++) {

            var colLetter = empty ? null : words[lineIndex].letters[colIndex];
            var cssClass;

            if(empty && match.wordGrid.state !== inProgressMatchState) {
                cssClass = "word_grid_disabled";
            } else if(empty) {
                cssClass = "word_grid_empty";
            } else {
                cssClass = "word_grid_" + colLetter.state.toLowerCase();
            }

            html += "<td class=\"" + cssClass + "\">" + (colLetter == null ? "<br>" : colLetter.letter.toUpperCase()) + "</td>";

        }

        html += "</tr>"

    }

    html += "</table>"

    document.getElementById("word_grid").innerHTML = html;

}

function sendGuess(form) {

    event.preventDefault();

    var word = form.input_guess.value;

    if(word.length == 0) {
        return;
    }

    if(word.length != match.settings.wordLength) {
        alert("Insira uma palavra com " + match.settings.wordLength + " letras.", "warning");
        return;
    }

    if(match.wordGrid != null) {

        if(match.wordGrid.state != inProgressMatchState) {
            return;
        }

        if(match.wordGrid.words.length >= match.settings.maxGuesses) {
            return;
        }

    }

    new Packet("newGuess", { "word": form.input_guess.value }).send();
    clearWordInput();

}

function alert(message, type, temporary = true) {

    closeAlert();

    var alertPlaceholder = document.getElementById('alert_placeholder');
    var wrapper = document.createElement('div');

    wrapper.innerHTML = [
        `<div id="alert" class="alert alert-${type} alert-dismissible" role="alert">`,
        `   <div>${message}</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        '</div>'
    ].join('');

    alertPlaceholder.append(wrapper);

    if(temporary) {
        alertTimeout = setTimeout(() => {
            closeAlert();
        }, 5000);
    }

}

function closeAlert() {
    var alert = new bootstrap.Alert('#alert');
    if(document.getElementById('alert') != null) {
        alert.close();
        clearTimeout(alertTimeout);
    }
}

function clearWordInput() {
    document.getElementById("input_guess").value = "";
}

class Packet {
    constructor(type, content) {
        this.uuid = match.uuid;
        this.type = type;
        this.content = content;
    }

    send() {
        socket.send(JSON.stringify(this));
    }
}

class MatchSettings {
    constructor(wordLength, maxGuesses) {
        this.wordLength = wordLength;
        this.maxGuesses = maxGuesses;
    }
}

class Match {
    constructor(uuid, settings, wordGrid) {
        this.uuid = uuid;
        this.settings = settings;
        this.wordGrid = wordGrid;
    }
}