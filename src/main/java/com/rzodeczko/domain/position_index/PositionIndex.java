package com.rzodeczko.domain.position_index;

import com.rzodeczko.domain.vo.Position;

public record PositionIndex(
        Position position,
        boolean isFree
) {

    public PositionIndex() {
        this(null, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Position getPosition() {
        return position;
    }

    public PositionIndex setPosition(Position position) {
        return new PositionIndex(position, isFree);
    }

    public boolean isFree() {
        return isFree;
    }

    public PositionIndex setFree(boolean free) {
        return new PositionIndex(position, free);
    }

    public static class Builder {
        private Position position;
        private boolean isFree;

        public Builder position(Position position) {
            this.position = position;
            return this;
        }

        public Builder isFree(boolean isFree) {
            this.isFree = isFree;
            return this;
        }

        public PositionIndex build() {
            return new PositionIndex(position, isFree);
        }
    }
}
