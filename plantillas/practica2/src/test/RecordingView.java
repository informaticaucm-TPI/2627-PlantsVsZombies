package test;

import pvz.view.GameView;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link GameView} that stores what commands ask it to show, so command tests can
 * assert on it without capturing {@code System.out}.
 */
public class RecordingView implements GameView {

    public final List<String> errors = new ArrayList<>();
    public final List<String> messages = new ArrayList<>();
    public int gamesShown;
    public int endMessagesShown;

    @Override
    public void showGame() {
        gamesShown++;
    }

    @Override
    public void showEndMessage() {
        endMessagesShown++;
    }

    @Override
    public void showError(String message) {
        errors.add(message);
    }

    @Override
    public void showMessage(String message) {
        messages.add(message);
    }

    @Override
    public String[] getPrompt() {
        return new String[] {""};
    }

    /** Lines of the last message shown with {@link #showMessage(String)}. */
    public List<String> lastMessageLines() {
        return messages.get(messages.size() - 1).lines().toList();
    }
}
