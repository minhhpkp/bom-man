package game.bomman.command.movingCommand;

import game.bomman.command.Command;
import game.bomman.entity.character.Character;

public class MoveDown extends Command {
    @Override
    public void executeOn(Character obj) {
        obj.moveDown();
    }
}