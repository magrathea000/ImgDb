package gui.components;

import model.ImgTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LastElementAutoCompleteField extends AutoCompleteField {

    public LastElementAutoCompleteField(int columns, boolean decorated) {
        super(columns, decorated);
    }

    public LastElementAutoCompleteField(int columns) {
        this(columns, true);
    }

    @Override
    protected List<ImgTag> getNewGuesses(String text, int pos) {
        if (text != null && !text.isBlank()) {
            if (text.charAt(text.length() - 1) == ' ') return new ArrayList<>(0);

            Set<String> enteredTags = Arrays.stream(text.trim().split("\\s+")).collect(Collectors.toSet());

            text = text.trim();
            int i = text.lastIndexOf(' ');
            if (i != -1) text = text.substring(i + 1);

            String finalText = text;

            return elements.stream()
                           .filter(e -> e.getName().startsWith(finalText))
                           .filter(e -> !enteredTags.contains(e.getName()))
                           .toList();
        }
        return new ArrayList<>(0);
    }

}
