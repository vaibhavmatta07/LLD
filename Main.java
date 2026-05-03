import java.util.Stack;

// -------------------- Core Abstraction --------------------

interface Editor {
    void insert(int position, String text);
    String delete(int position, int length);
    int getCursor();
    void setCursor(int position);
    int length();
    String getText();
}

// -------------------- Text Buffer --------------------

class TextBuffer implements Editor {
    private final StringBuilder content = new StringBuilder();
    private int cursor = 0;

    @Override
    public void insert(int position, String text) {
        position = clamp(position);
        content.insert(position, text);
    }

    @Override
    public String delete(int position, int length) {
        position = clamp(position);
        int start = Math.max(0, position - length);
        String deleted = content.substring(start, position);
        content.delete(start, position);
        return deleted;
    }

    @Override
    public int getCursor() {
        return cursor;
    }

    @Override
    public void setCursor(int position) {
        cursor = clamp(position);
    }

    @Override
    public int length() {
        return content.length();
    }

    @Override
    public String getText() {
        return content.toString();
    }

    private int clamp(int pos) {
        return Math.max(0, Math.min(pos, content.length()));
    }
}

// -------------------- Command Pattern --------------------

interface Command {
    void execute();
    void undo();
}

// -------------------- Concrete Commands --------------------

class TypeCommand implements Command {
    private final Editor editor;
    private final String text;
    private int position;

    public TypeCommand(Editor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        position = editor.getCursor();
        editor.insert(position, text);
        editor.setCursor(position + text.length());
    }

    @Override
    public void undo() {
        editor.delete(position + text.length(), text.length());
        editor.setCursor(position);
    }
}

class DeleteCommand implements Command {
    private final Editor editor;
    private final int length;
    private String deletedText;
    private int position;

    public DeleteCommand(Editor editor, int length) {
        this.editor = editor;
        this.length = length;
    }

    @Override
    public void execute() {
        position = editor.getCursor();
        deletedText = editor.delete(position, length);
        editor.setCursor(position - deletedText.length());
    }

    @Override
    public void undo() {
        editor.insert(position - deletedText.length(), deletedText);
        editor.setCursor(position);
    }
}

class MoveCursorCommand implements Command {
    private final Editor editor;
    private final int newPosition;
    private int oldPosition;

    public MoveCursorCommand(Editor editor, int newPosition) {
        this.editor = editor;
        this.newPosition = newPosition;
    }

    @Override
    public void execute() {
        oldPosition = editor.getCursor();
        editor.setCursor(newPosition);
    }

    @Override
    public void undo() {
        editor.setCursor(oldPosition);
    }
}

// -------------------- History Manager --------------------

class CommandHistory {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return true;
    }
}

// -------------------- Client --------------------

public class Main {
    public static void main(String[] args) {
        Editor editor = new TextBuffer();
        CommandHistory history = new CommandHistory();

        history.execute(new TypeCommand(editor, "Hello"));
        System.out.println(editor.getText());

        history.execute(new TypeCommand(editor, " World"));
        System.out.println(editor.getText());

        history.execute(new MoveCursorCommand(editor, 5));
        history.execute(new TypeCommand(editor, " Beautiful"));
        System.out.println(editor.getText());

        history.undo();
        System.out.println("Undo: " + editor.getText());

        history.undo();
        System.out.println("Undo: " + editor.getText());

        history.redo();
        System.out.println("Redo: " + editor.getText());

        history.execute(new DeleteCommand(editor, 3));
        System.out.println("Delete: " + editor.getText());

        history.undo();
        System.out.println("Undo Delete: " + editor.getText());
    }
}