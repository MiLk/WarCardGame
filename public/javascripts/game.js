'use strict';

const State = {
  Connecting: 0,
  StandBy: 1,
  JoiningQueue: 2,
  InQueue: 3,
  WaitingForStart: 4,
  InProgress: 5,
  WaitingNextTurn: 6,
  Disconnected: 7
};

class DOMHelper {
  static hideAll() {
    let bodyChildren = document.body.children;
    for (let i = 0; i < bodyChildren.length; ++i) {
      let el = bodyChildren[i];
      if (el.id && el.id !== 'messages') {
        el.style.display = 'none'
      }
    }
  }

  static show(id) {
    document.getElementById(id).style.display = 'block';
  }
}

class GameState {
  constructor() {
    this._state = State.Connecting;
    DOMHelper.hideAll();
  }

  setMessage(text) {
    if (!this._messageBox) {
      this._messageBox = document.getElementById('messages').firstElementChild;
    }
    this._messageBox.textContent = text;
  }

  setError(message, description) {
    if (!this._messageBox) {
      this._messageBox = document.getElementById('messages').firstElementChild;
    }
    this._messageBox.innerHTML = '<strong>' + message + '</strong><br />' + description;
  }

  changeState(state, msg) {
    switch (state) {
      case State.StandBy:
        DOMHelper.hideAll();
        DOMHelper.show('standby');
        break;
      case State.JoiningQueue:
        DOMHelper.hideAll();
        break;
      case State.InQueue:
        DOMHelper.hideAll();
        DOMHelper.show('inqueue');
        document.getElementById('inqueue').style.display = 'block';
        break;
      case State.WaitingForStart:
        DOMHelper.hideAll();
        break;
      case State.InProgress:
        DOMHelper.hideAll();
        DOMHelper.show('inprogress');
        break;
      case State.WaitingNextTurn:
        DOMHelper.hideAll();
        break;
      case State.Disconnected:
        DOMHelper.hideAll();
        msg = msg || 'You have been disconnected.';
        break;
      default:
        return;
    }
    this._state = state;
    this.setMessage(msg || '');
  }

  get state() {
    return this._state;
  }
}

function start() {
  // Game logic
  const gameState = new GameState();

  // WebSockets
  const ws = new WebSocket('ws://127.0.0.1:9000/ws');
  ws.onopen = () => gameState.changeState(State.StandBy);
  ws.onclose = () => gameState.changeState(State.Disconnected);
  ws.onerror = (evt) => console.error(evt);

  ws.onmessage = function (event) {
    try {
      let msg = JSON.parse(event.data);
      if (typeof msg === 'object' && msg.type && msg.message) {
        let type = msg.type;
        if (type === 'error' && msg.description) {
          gameState.setError(msg.message, msg.description);
        } else if(type === 'success' && msg.state) {
          let state = msg.state;
          if (State.hasOwnProperty(state)) {
            gameState.changeState(State[state], msg.message);
          }
        }
        return;
      }
    } catch (e) {
      console.error('unable to parse message', e);
    }
    console.log('message received:', event.data);
  };

  function doAction(action) {
    if (gameState.state == State.Connecting) {
      console.error("Connection in progress.");
      return;
    }

    if (gameState.state == State.Disconnected) {
      console.error("You have been disconnected.");
      return;
    }

    if (ws.readyState == 0) {
      console.error('The WebSocket connection is not ready.');
      return;
    }

    if (ws.readyState > 1) {
      gameState.changeState(State.Disconnected);
      return;
    }

    ws.send(JSON.stringify({action: action}));
  }

  document.getElementById('joinQueue').onclick = () => doAction('join');
  document.getElementById('leaveQueue').onclick = () => doAction('leave');
  document.getElementById('draw').onclick = () => doAction('draw');
}

document.addEventListener('DOMContentLoaded', start, false);
