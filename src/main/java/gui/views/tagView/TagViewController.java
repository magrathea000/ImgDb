package gui.views.tagView;

import database.DatabaseController;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class TagViewController implements gui.views.ViewController {

    private static TagViewController instance;

    final TagViewPanel v;
    final TagViewModel m;

    private TagViewController() {
        v = new TagViewPanel();
        m = new TagViewModel();
    }

    public static TagViewController get() {
        if (instance == null) {
            instance = new TagViewController();
        }
        return instance;
    }

    @Override
    public Component getComponent() {
        return v;
    }

    @Override
    public void activate() {
        log.info("{} activated", TagViewController.class.getSimpleName());
        v.setTags(DatabaseController.getInstance().getAllTags());
    }

}
