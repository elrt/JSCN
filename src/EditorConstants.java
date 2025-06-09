import java.awt.Color;

public class EditorConstants {
    public static final String[] COMMANDS = {
        "CP", "CI", "A", "I", "D", "X", "M", "P", "C", "S", "N", "B", "Y", "V", "E", "L", "G"
    };
    public static final String BRAINFUCK_COMMANDS = "><+-.,[]";
    public static final String[] BASIC_KEYWORDS = {
        "PRINT", "INPUT", "LET", "IF", "THEN", "ELSE", "ENDIF", "FOR", "TO", "STEP", "NEXT",
        "WHILE", "WEND", "DO", "LOOP", "UNTIL", "GOTO", "GOSUB", "RETURN", "DIM", "REM"
    };
    public static final String[] NASM_KEYWORDS = {
        "mov", "add", "sub", "mul", "imul", "div", "idiv", "jmp", "je", "jne", "jg",
        "jge", "jl", "jle", "call", "ret", "push", "pop", "and", "or", "xor", "not",
        "cmp", "test", "lea", "inc", "dec", "nop", "int",
        "eax", "ebx", "ecx", "edx", "esi", "edi", "esp", "ebp",
        "rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rsp", "rbp"
    };

    public static final Color COLOR_BACKGROUND = new Color(0xFFFFFF);
    public static final Color COLOR_DEFAULT = new Color(0x374151);
    public static final Color COLOR_COMMAND = new Color(0x3B82F6);
    public static final Color COLOR_INCLUDE = new Color(0x10B981);
    public static final Color COLOR_LABEL = new Color(0xF59E0B);
    public static final Color COLOR_COMMENT = new Color(0x6B7280);
    public static final Color COLOR_NUMBER = new Color(0x8B5CF6);
    public static final Color COLOR_CHAR = new Color(0xEF4444);
    public static final Color COLOR_STRING = new Color(0xEF4444);
    public static final Color COLOR_HEADER_TEXT = new Color(0x111827);
    public static final Color COLOR_CARD_BG = new Color(0xF3F4F6);
    public static final Color COLOR_ERROR_BG = new Color(0xFEE2E2);
    public static final Color COLOR_TAB_BG = new Color(0xF9FAFB);
    public static final Color COLOR_TAB_HOVER = new Color(0xE5E7EB);
    public static final Color COLOR_TAB_SELECTED = new Color(0xDBEAFE);
    public static final Color COLOR_STATUS_BAR = new Color(0xF9FAFB);
    public static final Color COLOR_SHADOW = new Color(0, 0, 0, 25);
}
