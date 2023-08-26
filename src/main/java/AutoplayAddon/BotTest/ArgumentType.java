
package AutoplayAddon.BotTest;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class ArgumentType<T> {
    public T parse(StringReader reader) throws CommandSyntaxException {
        return null;
    }

    public T parse(String input) throws IllegalArgumentException {
        return null;
    }

    public static class BooleanType extends ArgumentType<Boolean> {

        @Override
        public Boolean parse(String input) {
            return switch (input.toLowerCase()) {
                case "on", "enabled", "true" -> true;
                case "off", "disabled", "false" -> false;
                default -> throw new IllegalArgumentException("Invalid boolean value: " + input);
            };
        }
    }

    public static class PlayerNameType extends ArgumentType<String> {
        @Override
        public String parse(String input) {
            return input;
        }
    }

}
