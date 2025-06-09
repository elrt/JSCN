import javax.swing.text.*;
import java.util.regex.*;
import java.awt.Color;
import javax.swing.JTextPane; 

public class SyntaxHighlighter {
    private Style defaultStyle, commandStyle, includeStyle, labelStyle, commentStyle,
            numberStyle, charStyle, stringStyle, basicKeywordStyle, errorStyle;

    public SyntaxHighlighter() {
        initStyles();
    }

    private void initStyles() {
        StyledDocument dummyDoc = new DefaultStyledDocument();

        defaultStyle = dummyDoc.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, EditorConstants.COLOR_DEFAULT);
        StyleConstants.setFontFamily(defaultStyle, "Poppins");
        StyleConstants.setFontSize(defaultStyle, 16);

        commandStyle = dummyDoc.addStyle("command", null);
        StyleConstants.setForeground(commandStyle, EditorConstants.COLOR_COMMAND);
        StyleConstants.setBold(commandStyle, true);
        StyleConstants.setFontFamily(commandStyle, "Poppins");
        StyleConstants.setFontSize(commandStyle, 16);

        includeStyle = dummyDoc.addStyle("include", null);
        StyleConstants.setForeground(includeStyle, EditorConstants.COLOR_INCLUDE);
        StyleConstants.setFontFamily(includeStyle, "Poppins");
        StyleConstants.setFontSize(includeStyle, 16);

        labelStyle = dummyDoc.addStyle("label", null);
        StyleConstants.setForeground(labelStyle, EditorConstants.COLOR_LABEL);
        StyleConstants.setBold(labelStyle, true);
        StyleConstants.setFontFamily(labelStyle, "Poppins");
        StyleConstants.setFontSize(labelStyle, 16);

        commentStyle = dummyDoc.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, EditorConstants.COLOR_COMMENT);
        StyleConstants.setItalic(commentStyle, true);
        StyleConstants.setFontFamily(commentStyle, "Poppins");
        StyleConstants.setFontSize(commentStyle, 16);

        numberStyle = dummyDoc.addStyle("number", null);
        StyleConstants.setForeground(numberStyle, EditorConstants.COLOR_NUMBER);
        StyleConstants.setFontFamily(numberStyle, "Poppins");
        StyleConstants.setFontSize(numberStyle, 16);

        charStyle = dummyDoc.addStyle("char", null);
        StyleConstants.setForeground(charStyle, EditorConstants.COLOR_CHAR);
        StyleConstants.setFontFamily(charStyle, "Poppins");
        StyleConstants.setFontSize(charStyle, 16);

        stringStyle = dummyDoc.addStyle("string", null);
        StyleConstants.setForeground(stringStyle, EditorConstants.COLOR_STRING);
        StyleConstants.setFontFamily(stringStyle, "Poppins");
        StyleConstants.setFontSize(stringStyle, 16);

        basicKeywordStyle = dummyDoc.addStyle("basicKeyword", null);
        StyleConstants.setForeground(basicKeywordStyle, EditorConstants.COLOR_COMMAND);
        StyleConstants.setBold(basicKeywordStyle, true);
        StyleConstants.setFontFamily(basicKeywordStyle, "Poppins");
        StyleConstants.setFontSize(basicKeywordStyle, 16);

        errorStyle = dummyDoc.addStyle("error", null);
        StyleConstants.setBackground(errorStyle, EditorConstants.COLOR_ERROR_BG);
        StyleConstants.setFontFamily(errorStyle, "Poppins");
        StyleConstants.setFontSize(errorStyle, 16);
    }

    public Style getErrorStyle() {
        return errorStyle;
    }

    public void applySyntaxHighlighting(JTextPane textPane, KissEditor.EditorTab tab) {
        try {
            if (isBrainfuckFile(tab)) {
                applyBrainfuckHighlighting(textPane);
            } else if (isBasicFile(tab)) {
                applyBasicHighlighting(textPane);
            } else if (isNasmFile(tab)) {
                applyNasmHighlighting(textPane);
            } else {
                applyKissHighlighting(textPane);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBrainfuckFile(KissEditor.EditorTab tab) {
        if (tab == null || tab.file == null) return false;
        String name = tab.file.getName().toLowerCase();
        return name.endsWith(".b") || name.endsWith(".bf");
    }

    private boolean isBasicFile(KissEditor.EditorTab tab) {
        if (tab == null) return false;
        if(tab.file == null && tab.textPane != null){
            String text = tab.textPane.getText().toUpperCase();
            int countKeywords=0;
            for(String keyword : EditorConstants.BASIC_KEYWORDS){
                if(text.contains(keyword)) countKeywords++;
            }
            return countKeywords >= 3;
        }
        if (tab.file != null) {
            String name = tab.file.getName().toLowerCase();
            return name.endsWith(".bas") || name.endsWith(".basic");
        }
        return false;
    }

    private boolean isNasmFile(KissEditor.EditorTab tab) {
        if (tab == null || tab.file == null) return false;
        String name = tab.file.getName().toLowerCase();
        return name.endsWith(".asm") || name.endsWith(".nasm");
    }

    private void applyBasicHighlighting(JTextPane textPane) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            String text = textPane.getText();
            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);

            if (text.isEmpty()) return;

            int offset = 0;
            String[] lines = text.split("\\n", -1);

            Pattern keywordPattern;
            for (String line : lines) {
                int lineLength = line.length();
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty()) {
                    offset += lineLength + 1;
                    continue;
                }

                // Highlight comments starting with REM or '
                String upperTrimmed = trimmedLine.toUpperCase();
                if (upperTrimmed.startsWith("REM") || trimmedLine.startsWith("'")) {
                    doc.setCharacterAttributes(offset, lineLength, commentStyle, false);
                    offset += lineLength + 1;
                    continue;
                }

                // Highlight strings enclosed in double quotes
                int quoteStart = line.indexOf('"');
                while (quoteStart >= 0) {
                    int quoteEnd = line.indexOf('"', quoteStart + 1);
                    if (quoteEnd < 0) quoteEnd = lineLength - 1;
                    doc.setCharacterAttributes(offset + quoteStart, quoteEnd - quoteStart + 1, stringStyle, false);
                    quoteStart = line.indexOf('"', quoteEnd + 1);
                }

                // Highlight numbers using regex
                Pattern numberPattern = Pattern.compile("\\b\\d+\\.?\\d*\\b");
                Matcher numberMatcher = numberPattern.matcher(line);
                while (numberMatcher.find()) {
                    int start = numberMatcher.start();
                    int lenNum = numberMatcher.end() - numberMatcher.start();
                    doc.setCharacterAttributes(offset + start, lenNum, numberStyle, false);
                }

                // Highlight keywords - case insensitive
                for (String keyword : EditorConstants.BASIC_KEYWORDS) {
                    String upKeyword = keyword.toUpperCase();
                    // Use word boundary and case-insensitive matching
                    keywordPattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = keywordPattern.matcher(line);
                    while (matcher.find()) {
                        int start = matcher.start();
                        int lengthKw = matcher.end() - matcher.start();
                        doc.setCharacterAttributes(offset + start, lengthKw, basicKeywordStyle, false);
                    }
                }

                offset += lineLength + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyBrainfuckHighlighting(JTextPane textPane) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            String text = textPane.getText();
            int len = text.length();

            doc.setCharacterAttributes(0, len, defaultStyle, true);

            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (EditorConstants.BRAINFUCK_COMMANDS.indexOf(c) >= 0) {
                    doc.setCharacterAttributes(i, 1, commandStyle, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyNasmHighlighting(JTextPane textPane) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            String text = textPane.getText();
            int length = text.length();

            doc.setCharacterAttributes(0, length, defaultStyle, true);
            if (text.isEmpty()) return;

            String[] lines = text.split("\\n", -1);
            int offset = 0;

            Pattern commentPattern = Pattern.compile(";.*$");
            Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
            Pattern numberPattern = Pattern.compile("\\b(0x[\\da-fA-F]+|\\d+)\\b");

            for (String line : lines) {
                int lineLength = line.length();
                if (lineLength == 0) {
                    offset += 1;
                    continue;
                }

                Matcher commentMatcher = commentPattern.matcher(line);
                if (commentMatcher.find()) {
                    int start = offset + commentMatcher.start();
                    int lenComm = commentMatcher.end() - commentMatcher.start();
                    doc.setCharacterAttributes(start, lenComm, commentStyle, false);
                }

                Matcher stringMatcher = stringPattern.matcher(line);
                while (stringMatcher.find()) {
                    int start = offset + stringMatcher.start();
                    int end = stringMatcher.end();
                    doc.setCharacterAttributes(start, end - stringMatcher.start(), stringStyle, false);
                }

                Matcher numberMatcher = numberPattern.matcher(line);
                while (numberMatcher.find()) {
                    int start = offset + numberMatcher.start();
                    int lenNum = numberMatcher.end() - numberMatcher.start();
                    doc.setCharacterAttributes(start, lenNum, numberStyle, false);
                }

                String lowerLine = line.toLowerCase();
                for (String kw : EditorConstants.NASM_KEYWORDS) {
                    int idx = lowerLine.indexOf(kw.toLowerCase());
                    while (idx >= 0) {
                        boolean leftOk = idx == 0 || !Character.isLetterOrDigit(lowerLine.charAt(idx - 1));
                        int rightPos = idx + kw.length();
                        boolean rightOk = rightPos >= lowerLine.length() || !Character.isLetterOrDigit(lowerLine.charAt(rightPos));
                        if (leftOk && rightOk) {
                            doc.setCharacterAttributes(offset + idx, kw.length(), basicKeywordStyle, false);
                        }
                        idx = lowerLine.indexOf(kw.toLowerCase(), idx + kw.length());
                    }
                }
                offset += lineLength + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyKissHighlighting(JTextPane textPane) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            String text = textPane.getText();
            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);

            if (text.isEmpty()) return;

            int offset = 0;
            String[] lines = text.split("\\n", -1);

            for (String line : lines) {
                String trimmedLine = line.trim();
                int lineLength = line.length();

                if (trimmedLine.isEmpty()) {
                    offset += lineLength + 1;
                    continue;
                }

                if (trimmedLine.startsWith("#") && !trimmedLine.startsWith("#include")) {
                    doc.setCharacterAttributes(offset, lineLength, commentStyle, false);
                    offset += lineLength + 1;
                    continue;
                }

                if (trimmedLine.startsWith("#include")) {
                    int includeIndex = line.indexOf("#include");
                    if (includeIndex >= 0) {
                        int incLength = "#include".length();
                        doc.setCharacterAttributes(offset + includeIndex, incLength, includeStyle, false);
                        int restLength = lineLength - (includeIndex + incLength);
                        if (restLength > 0) {
                            doc.setCharacterAttributes(offset + includeIndex + incLength, restLength, includeStyle, false);
                        }
                    }
                    offset += lineLength + 1;
                    continue;
                }

                if (trimmedLine.startsWith(":")) {
                    int firstSpace = trimmedLine.indexOf(' ');
                    int labelLen;
                    if (firstSpace == -1) labelLen = lineLength;
                    else labelLen = firstSpace;
                    doc.setCharacterAttributes(offset, labelLen, labelStyle, false);
                }

                for (String cmd : EditorConstants.COMMANDS) {
                    if (trimmedLine.startsWith(cmd)) {
                        int cmdIndex = line.indexOf(cmd);
                        if (cmdIndex >= 0) {
                            doc.setCharacterAttributes(offset + cmdIndex, cmd.length(), commandStyle, false);
                        }
                        break;
                    }
                }

                int searchFrom = 0;
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    if (token.isEmpty()) continue;
                    int pos = line.indexOf(token, searchFrom);
                    if (pos < 0) continue;
                    searchFrom = pos + token.length();

                    if (token.matches("-?\\d+")) {
                        doc.setCharacterAttributes(offset + pos, token.length(), numberStyle, false);
                    } else if (token.matches("'[^']'")) {
                        doc.setCharacterAttributes(offset + pos, token.length(), charStyle, false);
                    }
                }

                offset += lineLength + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


