import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
    private int index;
    private int line;
    private int column;

    public List<Token> tokenize(String source) {
        input = source;
        index = 0;
        line = 1;
        column = 1;
        List<Token> tokens = new ArrayList<Token>();

        while (!isAtEnd()) {
            char c = peek();

            if (Character.isWhitespace(c)) {
                consumeWhitespace();
            } else if (c == '-' && peekNext() == '-') {
                consumeLineComment();
            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            } else if (c == '\'') {
                tokens.add(readString());
            } else {
                tokens.add(readSymbol());
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private boolean isAtEnd() {
        return index >= input.length();
    }

    private char peek() {
        return input.charAt(index);
    }

    private char peekNext() {
        return index + 1 < input.length() ? input.charAt(index + 1) : '\0';
    }

    private char advance() {
        char c = input.charAt(index++);

        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        return c;
    }

    private void consumeWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private void consumeLineComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    private Token readIdentifierOrKeyword() {
        int startLine = line;
        int startColumn = column;
        StringBuilder text = new StringBuilder();

        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            text.append(advance());
        }

        String value = text.toString();
        String upper = value.toUpperCase();

        if (upper.equals("SELECT")) return new Token(TokenType.SELECT, value, startLine, startColumn);
        if (upper.equals("FROM")) return new Token(TokenType.FROM, value, startLine, startColumn);
        if (upper.equals("WHERE")) return new Token(TokenType.WHERE, value, startLine, startColumn);
        if (upper.equals("AND")) return new Token(TokenType.AND, value, startLine, startColumn);
        if (upper.equals("OR")) return new Token(TokenType.OR, value, startLine, startColumn);
        if (upper.equals("TRUE")) return new Token(TokenType.TRUE, value, startLine, startColumn);
        if (upper.equals("FALSE")) return new Token(TokenType.FALSE, value, startLine, startColumn);

        return new Token(TokenType.IDENTIFIER, value, startLine, startColumn);
    }

    private Token readNumber() {
        int startLine = line;
        int startColumn = column;
        StringBuilder text = new StringBuilder();

        while (!isAtEnd() && Character.isDigit(peek())) {
            text.append(advance());
        }

        return new Token(TokenType.NUMBER, text.toString(), startLine, startColumn);
    }

    private Token readString() {
        int startLine = line;
        int startColumn = column;
        advance();

        StringBuilder text = new StringBuilder();

        while (!isAtEnd() && peek() != '\'') {
            text.append(advance());
        }

        if (!isAtEnd()) {
            advance();
        }

        return new Token(TokenType.STRING, text.toString(), startLine, startColumn);
    }

    private Token readSymbol() {
        int startLine = line;
        int startColumn = column;
        char c = advance();

        if (c == ',') return new Token(TokenType.COMMA, ",", startLine, startColumn);
        if (c == '*') return new Token(TokenType.STAR, "*", startLine, startColumn);
        if (c == ';') return new Token(TokenType.SEMICOLON, ";", startLine, startColumn);

        if (c == '=' && !isAtEnd() && peek() == '=') {
            advance();
            return new Token(TokenType.EQUAL, "==", startLine, startColumn);
        }

        if (c == '!') {
            if (!isAtEnd() && peek() == '=') {
                advance();
                return new Token(TokenType.NOT_EQUAL, "!=", startLine, startColumn);
            }

            return new Token(TokenType.INVALID, String.valueOf(c), startLine, startColumn);
        }

        if (c == '=') return new Token(TokenType.EQUAL, "=", startLine, startColumn);

        if (c == '>' && !isAtEnd() && peek() == '=') {
            advance();
            return new Token(TokenType.GREATER_EQUAL, ">=", startLine, startColumn);
        }

        if (c == '<' && !isAtEnd() && peek() == '=') {
            advance();
            return new Token(TokenType.LESS_EQUAL, "<=", startLine, startColumn);
        }

        if (c == '<' && !isAtEnd() && peek() == '>') {
            advance();
            return new Token(TokenType.NOT_EQUAL, "<>", startLine, startColumn);
        }

        if (c == '>') return new Token(TokenType.GREATER, ">", startLine, startColumn);
        if (c == '<') return new Token(TokenType.LESS, "<", startLine, startColumn);

        return new Token(TokenType.INVALID, String.valueOf(c), startLine, startColumn);
    }
}