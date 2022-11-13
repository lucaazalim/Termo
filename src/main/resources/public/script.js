var socket;
var match;

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
            var words = match.wordGrid.words;
            var html = "<table style=\"width:100%\">";

            for(index = 0; index < match.settings.maxGuesses; index++) {

                html += "<tr>";

                if(index < words.length) {

                    var word = words[index];

                    for(const letter of word.letters) {
                        var color = letter.state === "NOT_PRESENT" ? "gray" : (letter.state === "OUT_OF_POSITION" ? "yellow" : "green")
                        html += "<td bgcolor=" + color + ">" + letter.letter + "</td>";
                    }

                }

                html += "</tr>"

            }

            html += "</table>"

            document.getElementById("word_grid").innerHTML = html;

        }

    }

}

function sendGuess(form) {

    event.preventDefault();

    var word = form.input_guess.value;

    if(word.length != match.settings.wordLength) {
        return;
    }

    if(match.wordGrid != null) {

        if(match.wordGrid.state != "IN_PROGRESS") {
            //var myModal = new bootstrap.Modal(document.getElementById("modal-test"), {});
            //myModal.show();
            return;
        }

        if(match.wordGrid.words.length >= match.settings.maxGuesses) {
            return;
        }

    }

    new Packet("newGuess", {
        "word": form.input_guess.value
    }).send();

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