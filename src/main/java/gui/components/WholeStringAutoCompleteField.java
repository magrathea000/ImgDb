package gui.components;

import model.ImgTag;

import java.util.List;

public class WholeStringAutoCompleteField extends AutoCompleteField {

    public WholeStringAutoCompleteField(int columns) {
        this(columns, true);
    }

    public WholeStringAutoCompleteField(int columns, boolean decorated) {
        super(columns, decorated);
    }

    @Override
    protected List<ImgTag> getNewGuesses(String text, int pos) {
        return elements.stream().filter(e -> e.getName().startsWith(text)).toList();
    }
}
