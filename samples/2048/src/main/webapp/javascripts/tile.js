function Tile(position, value, previousPosition, mergedFrom) {
  this.x                = position.x;
  this.y                = position.y;
  this.value            = value || 2;

  this.previousPosition = previousPosition;
  this.mergedFrom       = mergedFrom; // Tracks tiles that merged together
}

Tile.prototype.savePosition = function () {
  this.previousPosition = { x: this.x, y: this.y };
};

Tile.prototype.updatePosition = function (position) {
  this.x = position.x;
  this.y = position.y;
};

Tile.prototype.serialize = function () {
  return {
    position: {
      x: this.x,
      y: this.y
    },
    value: this.value,
    previousPosition: this.previousPosition,
    mergedFrom: this.mergedFrom
  };
};
