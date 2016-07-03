'use strict';

const State = {
  Connecting: 0,
  StandBy: 1,
  InQueue: 2,
  InGame: 3,
  Disconnected: 4
};

class GameState {
  constructor() {
    this._state = State.Connecting;
    document.getElementById('connecting').style.display = 'block';
  }

  changeState(state) {
    console.log('changeState', state);
    switch (state) {
      case State.StandBy:
        document.getElementById('connecting').style.display = 'none';
        document.getElementById('standby').style.display = 'block';
        break;
      case State.InQueue:
        document.getElementById('standby').style.display = 'none';
        document.getElementById('inqueue').style.display = 'block';
        break;
      case State.InGame:
        document.getElementById('inqueue').style.display = 'none';
        document.getElementById('ingame').style.display = 'block';
        break;
      case State.Disconnected:
        let bodyChildren = document.body.children;
        for (let i = 0; i < bodyChildren.length; ++i) {
          bodyChildren[i].style.display = 'none'
        }
        document.getElementById('disconnected').style.display = 'block';
        break;
      default:
        return;
    }
    this._state = state;
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
      if (typeof msg === 'object' && msg.state && msg.msg) {
        let state = msg.state;
        if (State.hasOwnProperty(state)) {
          gameState.changeState(State[state]);
        }
        console.log('message received:', msg.msg);
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

    if (ws.readyState != 1) {
      console.error('The WebSocket connection is not ready or already closed.');
      return;
    }

    ws.send(JSON.stringify({action: action}));
  }

  document.getElementById('joinQueue').onclick = () => doAction('join');
  document.getElementById('leaveQueue').onclick = () => doAction('leave');
  document.getElementById('draw').onclick = () => doAction('draw');
}

document.addEventListener('DOMContentLoaded', start, false);
