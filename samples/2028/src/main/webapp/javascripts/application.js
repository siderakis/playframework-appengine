// Wait till the browser is ready to render the game (avoids glitches)
//window .requestAnimationFrame(function () {
  var GAME = new GameManager(4, KeyboardInputManager, HTMLActuator, LocalStorageManager);
//});
