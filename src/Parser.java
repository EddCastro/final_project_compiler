import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;
    private ValidationResult result;

    public SelectStatement parse(List<Token> tokens, ValidationResult result) {
        this.tokens = tokens;
        this.pos = 0;
        this.result = result;

        SelectStatement statement = new SelectStatement();

        expect(TokenType.SELECT, "SYNTACTIC_EXPECTED_SELECT");
        parseColumns(statement);
        expect(TokenType.FROM, "SYNTACTIC_EXPECTED_FROM");

        Token table = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_TABLE");
        if (table != null) statement.table = table.lexeme;

        if (match(TokenType.WHERE)) {
            parseWhere(statement);
        }

        if (check(TokenType.SEMICOLON)) advance();

        if (!check(TokenType.EOF)) {
            result.diagnostics.add(new Diagnostic(
                "SYNTACTIC_UNEXPECTED_TOKEN",
                "Token inesperado: " + current().lexeme,
                current().span));
        }

        return statement;
    }

    private void parseColumns(SelectStatement statement) {
        if (match(TokenType.STAR)) {
            statement.columns.add("*");
            return;
        }

        Token first = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
        if (first != null) statement.columns.add(first.lexeme);

        while (match(TokenType.COMMA)) {
            Token next = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
            if (next != null) statement.columns.add(next.lexeme);
        }
    }

    private void parseWhere(SelectStatement statement) {
        ConditionChain chain = new ConditionChain();

        WhereCondition first = parseCondition();
        if (first == null) {
            statement.where = chain;
            return;
        }

        chain.conditions.add(first);

        while (check(TokenType.AND) || check(TokenType.OR)) {
            Token connector = advance();
            WhereCondition next = parseCondition();

            if (next == null) {
                break;
            }

            chain.connectors.add(connector.lexeme.toUpperCase());
            chain.conditions.add(next);
        }

        statement.where = chain;
    }

    private WhereCondition parseCondition() {
        Token column = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_WHERE_OPERAND");
        if (column == null) return null;

        Token operator = parseOperator();
        if (operator == null) return null;

        Token literal = parseLiteral();
        if (literal == null) return null;

        return new WhereCondition(
            column.lexeme,
            operator.lexeme,
            literal.lexeme,
            getLiteralType(literal),
            column.span,
            operator.span,
            literal.span);
    }

    private Token parseOperator() {
        if (isOperator(current().type)) {
            return advance();
        }

        result.diagnostics.add(new Diagnostic(
            "SYNTACTIC_EXPECTED_WHERE_OPERATOR",
            "Se esperaba operador en WHERE",
            current().span));

        return null;
    }

    private Token parseLiteral() {
        if (check(TokenType.NUMBER) || check(TokenType.STRING) || check(TokenType.TRUE) || check(TokenType.FALSE)) {
            return advance();
        }

        result.diagnostics.add(new Diagnostic(
            "SYNTACTIC_EXPECTED_WHERE_OPERAND",
            "Se esperaba literal en WHERE",
            current().span));

        return null;
    }

    private LiteralType getLiteralType(Token token) {
        if (token.type == TokenType.NUMBER) return LiteralType.NUMBER;
        if (token.type == TokenType.STRING) return LiteralType.STRING;
        if (token.type == TokenType.TRUE || token.type == TokenType.FALSE) return LiteralType.BOOLEAN;
        return LiteralType.UNKNOWN;
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.EQUAL
            || type == TokenType.GREATER
            || type == TokenType.LESS
            || type == TokenType.GREATER_EQUAL
            || type == TokenType.LESS_EQUAL
            || type == TokenType.NOT_EQUAL;
    }

    private Token expect(TokenType type, String code) {
        if (check(type)) return advance();

        result.diagnostics.add(new Diagnostic(
            code,
            "Se esperaba " + type + " y se encontro " + current().type,
            current().span));

        return null;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }

        return false;
    }

    private boolean check(TokenType type) {
        return current().type == type;
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token advance() {
        return tokens.get(pos++);
    }
}