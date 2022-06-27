package gui.views.imageView;

import database.DatabaseController;
import gui.components.WholeStringAutoCompleteField;
import model.ImgTag;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NewTagDialog {

    private static final WholeStringAutoCompleteField auto = new WholeStringAutoCompleteField(35, true);

    private static JDialog build(Frame owner, String title, String message, Set<ImgTag> suggestions, Result result) {
        JDialog d = new JDialog(owner, title, true);

        JPanel p = new JPanel(new BorderLayout(), true);

        JPanel pcont = new JPanel(new FlowLayout(), true);

        pcont.add(new JLabel("New tag:"));

        auto.setElements(suggestions);
        auto.setReturnAction(t -> {
            auto.setText(t.trim().toLowerCase().replaceAll("\\s+", "_"));
        });

        pcont.add(auto);

        JPanel pctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton ok = new JButton("Ok");

        ok.addActionListener(e -> {
            result.tag = auto.getText().trim().toLowerCase().replaceAll("\\s+", "_");
            d.setVisible(false);
            d.dispose();
        });

        JButton cancel = new JButton("Cancel");

        cancel.addActionListener(e -> {
            result.tag = null;
            d.setVisible(false);
            d.dispose();
        });

        pctrl.add(ok);
        pctrl.add(cancel);

        p.add(new JLabel(message), BorderLayout.NORTH);
        p.add(pcont, BorderLayout.CENTER);
        p.add(pctrl, BorderLayout.SOUTH);

        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        p.setPreferredSize(new Dimension(500, 125));

        d.getContentPane().add(p);
        d.setResizable(false);
        d.pack();
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setLocationRelativeTo(owner);

        return d;
    }

    public static String showArtistDialog(Frame owner, final List<ImgTag> imageTags) {

        String title   = "Add new artist tag...";
        String message = "Add new artist tag.";

        DatabaseController dbc = DatabaseController.getInstance();

        ImgTag.Category cat = ImgTag.Category.Artist;

        var suggestions = dbc.getAllTags()
                             .stream()
                             .filter(i -> i.getCategory() == cat)
                             .filter(i -> !imageTags.contains(i))
                             .collect(Collectors.toSet());

        Result result = new Result();

        JDialog d = build(owner, title, message, suggestions, result);
        d.setVisible(true);

        String prefix = cat.getShortName() + ":";

        if (result.tag == null) return null;

        return result.tag.startsWith(prefix) ? result.tag : prefix + result.tag;
    }

    public static String showCharacterDialog(Frame owner, final List<ImgTag> imageTags) {

        String title   = "Add new character tag...";
        String message = "Add new character tag.";

        DatabaseController dbc = DatabaseController.getInstance();

        ImgTag.Category cat = ImgTag.Category.Character;

        var suggestions = dbc.getAllTags()
                             .stream()
                             .filter(i -> i.getCategory() == cat)
                             .filter(i -> !imageTags.contains(i))
                             .collect(Collectors.toSet());

        Result result = new Result();

        JDialog d = build(owner, title, message, suggestions, result);
        d.setVisible(true);

        String prefix = cat.getShortName() + ":";

        if (result.tag == null) return null;

        return result.tag.startsWith(prefix) ? result.tag : prefix + result.tag;
    }

    public static String showGeneralDialog(Frame owner, final List<ImgTag> imageTags) {

        String title   = "Add new general tag...";
        String message = "Add new general tag.";

        DatabaseController dbc = DatabaseController.getInstance();

        ImgTag.Category cat = ImgTag.Category.General;

        var suggestions = dbc.getAllTags()
                             .stream()
                             .filter(i -> i.getCategory() == cat)
                             .filter(i -> !imageTags.contains(i))
                             .collect(Collectors.toSet());

        Result result = new Result();

        JDialog d = build(owner, title, message, suggestions, result);
        d.setVisible(true);

        String prefix = cat.getShortName() + ":";

        if (result.tag == null) return null;

        return result.tag.startsWith(prefix) ? result.tag : prefix + result.tag;
    }

    public static String showMetaDialog(Frame owner, final List<ImgTag> imageTags) {

        String title   = "Add new meta tag...";
        String message = "Add new meta tag.";

        DatabaseController dbc = DatabaseController.getInstance();

        ImgTag.Category cat = ImgTag.Category.Meta;

        var suggestions = dbc.getAllTags()
                             .stream()
                             .filter(i -> i.getCategory() == cat)
                             .filter(i -> !imageTags.contains(i))
                             .collect(Collectors.toSet());

        Result result = new Result();

        JDialog d = build(owner, title, message, suggestions, result);
        d.setVisible(true);

        String prefix = cat.getShortName() + ":";

        if (result.tag == null) return null;

        return result.tag.startsWith(prefix) ? result.tag : prefix + result.tag;
    }

    private static class Result {
        public String tag;
    }
}
