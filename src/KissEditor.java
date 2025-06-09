import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class KissEditor extends JFrame {
    private JTabbedPane tabbedPane;
    private List<EditorTab> tabs = new ArrayList<>();
    private JLabel statusBar;
    private int newTabCounter = 1;
    private SyntaxHighlighter highlighter;

    public class EditorTab {
        JTextPane textPane;
        File file;
        Set<Integer> errorLines = new HashSet<>();
        boolean modified = false;

        final DocumentListener lineNumberUpdater = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshLineNumbers(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshLineNumbers(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshLineNumbers(); }
            private void refreshLineNumbers() {
                JScrollPane pane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, textPane);
                if (pane != null) {
                    Component rowHeader = pane.getRowHeader().getView();
                    if (rowHeader instanceof LineNumberView) {
                        ((LineNumberView) rowHeader).revalidate();
                        ((LineNumberView) rowHeader).repaint();
                    }
                }
            }
        };
    }

    public KissEditor() {
        super("Just Simple Code Notepad (JSCN)");
        highlighter = new SyntaxHighlighter();
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(EditorConstants.COLOR_BACKGROUND);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        tabbedPane = new JTabbedPane() {
            @Override
            public void setSelectedIndex(int index) {
                super.setSelectedIndex(index);
                updateStatusBar();
                updateHighlighting(getCurrentTab());
            }
        };
        tabbedPane.setBackground(EditorConstants.COLOR_TAB_BG);
        tabbedPane.setForeground(EditorConstants.COLOR_DEFAULT);
        tabbedPane.setFont(new Font("Poppins", Font.PLAIN, 14));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabbedPane.setOpaque(true);

        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = EditorConstants.COLOR_TAB_SELECTED;
                lightHighlight = EditorConstants.COLOR_TAB_HOVER;
                shadow = EditorConstants.COLOR_SHADOW;
                darkShadow = EditorConstants.COLOR_SHADOW;
                focus = EditorConstants.COLOR_TAB_SELECTED;
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setPaint(EditorConstants.COLOR_TAB_SELECTED);
                } else {
                    g2.setPaint(EditorConstants.COLOR_TAB_BG);
                }
                g2.fillRoundRect(x, y, w, h, 12, 12);
            }
        });

        statusBar = new JLabel(" Готово");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusBar.setFont(new Font("Poppins", Font.PLAIN, 12));
        statusBar.setForeground(EditorConstants.COLOR_DEFAULT);
        statusBar.setBackground(EditorConstants.COLOR_STATUS_BAR);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(statusBar.getWidth(), 28));
        statusBar.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            protected void paintEnabledText(JLabel l, Graphics g, String s, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintEnabledText(l, g, s, x, y);
            }
        });
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                statusBar.getBorder(),
                BorderFactory.createEmptyBorder(0, 5, 0, 0)
        ));

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(EditorConstants.COLOR_STATUS_BAR);
        statusPanel.add(statusBar, BorderLayout.CENTER);

        containerPanel.add(tabbedPane, BorderLayout.CENTER);
        containerPanel.add(statusPanel, BorderLayout.SOUTH);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(EditorConstants.COLOR_BACKGROUND);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        rootPanel.add(createHeader(), BorderLayout.NORTH);
        rootPanel.add(containerPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);

        addNewTab("Untitled", null);
        setupKeyBindings();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(EditorConstants.COLOR_TAB_BG);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setFont(new Font("Poppins", Font.PLAIN, 14));
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("Новый", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> addNewTab("Untitled " + (newTabCounter++), null));

        JMenuItem openItem = new JMenuItem("Открыть...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> loadFile());

        JMenuItem saveItem = new JMenuItem("Сохранить", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveFile());

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Правка");
        editMenu.setFont(new Font("Poppins", Font.PLAIN, 14));
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem cutItem = new JMenuItem("Вырезать");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(e -> getCurrentTextPane().cut());

        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(e -> getCurrentTextPane().copy());

        JMenuItem pasteItem = new JMenuItem("Вставить");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> getCurrentTextPane().paste());

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        return menuBar;
    }

    private JComponent createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EditorConstants.COLOR_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        JLabel header = new JLabel("Just Simple Code Notepad (JSCN)", SwingConstants.CENTER);
        header.setFont(new Font("Poppins", Font.BOLD, 56));
        header.setForeground(EditorConstants.COLOR_HEADER_TEXT);

        JLabel subHeader = new JLabel("Подсветка для BASIC, Brainfuck, KISS и NASM", SwingConstants.CENTER);
        subHeader.setFont(new Font("Poppins", Font.PLAIN, 18));
        subHeader.setForeground(EditorConstants.COLOR_COMMENT);

        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(subHeader, BorderLayout.SOUTH);
        return headerPanel;
    }

    private void addNewTab(String title, File file) {
        EditorTab tab = new EditorTab();
        tab.textPane = new JTextPane();
        tab.file = file;

        tab.textPane.setFont(new Font("Poppins", Font.PLAIN, 16));
        tab.textPane.setBackground(EditorConstants.COLOR_BACKGROUND);
        tab.textPane.setForeground(EditorConstants.COLOR_DEFAULT);
        tab.textPane.setCaretColor(EditorConstants.COLOR_DEFAULT);
        tab.textPane.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        tab.textPane.setMargin(new Insets(8, 8, 8, 8));

        tab.textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                tab.modified = true;
                updateTabTitle(tab);
                updateHighlighting(tab);
            }

            public void removeUpdate(DocumentEvent e) {
                tab.modified = true;
                updateTabTitle(tab);
                updateHighlighting(tab);
            }

            public void changedUpdate(DocumentEvent e) { }
        });

        tab.textPane.getDocument().addDocumentListener(tab.lineNumberUpdater);
        tab.textPane.addCaretListener(e -> updateStatusBar());

        JScrollPane scrollPane = new JScrollPane(tab.textPane);
        scrollPane.getViewport().setBackground(EditorConstants.COLOR_BACKGROUND);
        LineNumberView lineNumberView = new LineNumberView(tab.textPane);
        scrollPane.setRowHeaderView(lineNumberView);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xE5E7EB)));
        scrollPane.setPreferredSize(new Dimension(1000, 580));

        JPanel tabHeader = new JPanel(new BorderLayout());
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Poppins", Font.PLAIN, 13));

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Poppins", Font.PLAIN, 16));
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(24, 24));
        closeButton.addActionListener(e -> closeTab(tabbedPane.indexOfComponent(scrollPane)));

        tabHeader.add(titleLabel, BorderLayout.CENTER);
        tabHeader.add(closeButton, BorderLayout.EAST);
        tabHeader.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabHeader);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

        tabs.add(tab);
        setupDocumentListener(tab);
        updateStatusBar();
        updateHighlighting(tab);
    }

    private void updateTabTitle(EditorTab tab) {
        int index = tabs.indexOf(tab);
        if (index >= 0) {
            Component tabComp = tabbedPane.getTabComponentAt(index);
            if (tabComp instanceof JPanel) {
                JPanel header = (JPanel) tabComp;
                Component[] comps = header.getComponents();
                for (Component c : comps) {
                    if (c instanceof JLabel) {
                        String title = tab.file != null ? tab.file.getName() : "Untitled";
                        if (tab.modified) title += " *";
                        ((JLabel) c).setText(title);
                        break;
                    }
                }
            }
        }
    }

    private void setupDocumentListener(EditorTab tab) {
        tab.textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateHighlighting(tab);
            }

            public void removeUpdate(DocumentEvent e) {
                updateHighlighting(tab);
            }

            public void changedUpdate(DocumentEvent e) { }
        });
    }

    private void updateHighlighting(EditorTab tab) {
        if (tab == null) return;
        SwingUtilities.invokeLater(() -> {
            highlighter.applySyntaxHighlighting(tab.textPane, tab);
            highlightErrors(tab);
        });
    }

    private void highlightErrors(EditorTab tab) {
        try {
            StyledDocument doc = tab.textPane.getStyledDocument();

            highlighter.applySyntaxHighlighting(tab.textPane, tab);

            for (int lineNum : tab.errorLines) {
                Element root = doc.getDefaultRootElement();
                if (lineNum >= 1 && lineNum <= root.getElementCount()) {
                    Element lineElem = root.getElement(lineNum - 1);
                    doc.setCharacterAttributes(
                            lineElem.getStartOffset(),
                            lineElem.getEndOffset() - lineElem.getStartOffset() - 1,
                            highlighter.getErrorStyle(),
                            false
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabs.size()) return;

        EditorTab tab = tabs.get(tabIndex);

        if (tab.modified) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Сохранить изменения перед закрытием?", "Закрытие вкладки",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                saveFile(tabIndex);
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        tabbedPane.remove(tabIndex);
        tabs.remove(tabIndex);

        if (tabs.isEmpty()) {
            addNewTab("Untitled " + (newTabCounter++), null);
        }
    }

    private void loadFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            addNewTab(file.getName(), file);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                EditorTab tab = tabs.get(tabs.size() - 1);
                tab.textPane.read(reader, null);
                tab.modified = false;
                updateTabTitle(tab);
                updateHighlighting(tab);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки файла:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        saveFile(tabbedPane.getSelectedIndex());
    }

    private void saveFile(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabs.size()) return;

        EditorTab tab = tabs.get(tabIndex);

        if (tab.file == null) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                tab.file = chooser.getSelectedFile();
                tabbedPane.setTitleAt(tabIndex, tab.file.getName());
                updateTabTitle(tab);
            } else {
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tab.file))) {
            tab.textPane.write(writer);
            tab.modified = false;
            updateTabTitle(tab);
            statusBar.setText(" Файл сохранен: " + tab.file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка сохранения файла:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private EditorTab getCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index >= 0 && index < tabs.size()) {
            return tabs.get(index);
        }
        return null;
    }

    private JTextPane getCurrentTextPane() {
        EditorTab tab = getCurrentTab();
        return tab != null ? tab.textPane : null;
    }

    private void updateStatusBar() {
        JTextPane textPane = getCurrentTextPane();
        if (textPane != null) {
            try {
                int caretPos = textPane.getCaretPosition();
                int line = 1;
                int column = 1;

                Document doc = textPane.getDocument();
                if (doc != null) {
                    int offset = caretPos;
                    if (offset >= 0) {
                        Element root = doc.getDefaultRootElement();
                        line = root.getElementIndex(offset) + 1;
                        Element lineElem = root.getElement(line - 1);
                        column = offset - lineElem.getStartOffset() + 1;
                    }
                }

                String fileInfo = "";
                EditorTab tab = getCurrentTab();
                if (tab != null && tab.file != null) {
                    fileInfo = " | Файл: " + tab.file.getName();
                }

                statusBar.setText(String.format(" Строка: %d, Колонка: %d%s", line, column, fileInfo));
            } catch (Exception e) {
                statusBar.setText(" Готово");
            }
        } else {
            statusBar.setText(" Готово");
        }
    }

    private void setupKeyBindings() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        am.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
        am.put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "new");
        am.put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTab("Untitled " + (newTabCounter++), null);
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "close");
        am.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeTab(tabbedPane.getSelectedIndex());
            }
        });
    }

    private class LineNumberView extends JComponent {
        private final JTextPane textPane;
        private final Font font;
        private final int MARGIN = 5;

        public LineNumberView(JTextPane textPane) {
            this.textPane = textPane;
            this.font = new Font("Poppins", Font.PLAIN, 14);
            setFont(font);
            setBackground(EditorConstants.COLOR_CARD_BG);
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xE5E7EB)));

            textPane.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { repaint(); }
                public void removeUpdate(DocumentEvent e) { repaint(); }
                public void changedUpdate(DocumentEvent e) { repaint(); }
            });

            textPane.addCaretListener(e -> repaint());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(font);
            
            Rectangle visibleRect = textPane.getVisibleRect();
            int startY = visibleRect.y;
            int endY = startY + visibleRect.height;
            
            FontMetrics fm = g2.getFontMetrics();
            int baseline = fm.getAscent();
            
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(EditorConstants.COLOR_COMMENT);
            
            try {
                Element root = textPane.getDocument().getDefaultRootElement();
                int startLine = root.getElementIndex(textPane.viewToModel(new Point(0, startY)));
                int endLine = root.getElementIndex(textPane.viewToModel(new Point(0, endY))) + 1;
                
                startLine = Math.max(0, startLine);
                endLine = Math.min(root.getElementCount() - 1, endLine);
                
                for (int i = startLine; i <= endLine; i++) {
                    Element line = root.getElement(i);
                    Rectangle r = textPane.modelToView(line.getStartOffset());
                    
                    if (r != null) {
                        String number = String.valueOf(i + 1);
                        int x = getWidth() - fm.stringWidth(number) - MARGIN;
                        int y = r.y + baseline;
                        g2.drawString(number, x, y);
                    }
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            Element root = textPane.getDocument().getDefaultRootElement();
            int lineCount = root.getElementCount();
            int maxDigits = Math.max(3, String.valueOf(lineCount).length());
            FontMetrics fm = getFontMetrics(font);
            int width = fm.stringWidth("0") * maxDigits + 2 * MARGIN;
            return new Dimension(width, textPane.getHeight());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                try {
                    Font poppins = Font.createFont(Font.TRUETYPE_FONT, new File("Poppins-Regular.ttf")).deriveFont(14f);
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    ge.registerFont(poppins);
                } catch (Exception ignore) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            KissEditor editor = new KissEditor();
            editor.setVisible(true);
        });
    }
}
