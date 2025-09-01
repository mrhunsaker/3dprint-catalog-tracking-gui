import javax.swing.*;

public class SearchDialog extends JDialog {
    public SearchDialog(JFrame parent) {
        super(parent, "Search Projects", true);
        add(new JLabel("Search Dialog Placeholder"));
        setSize(300, 200);
    }
}
