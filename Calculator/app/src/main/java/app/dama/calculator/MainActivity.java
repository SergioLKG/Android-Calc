package app.dama.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private String aux = "";
    private String calc = "";
    private String last = "";
    private double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvTotal = findViewById(R.id.tv_total);
        TextView tvLast = findViewById(R.id.tv_last);
        Button btnClear = findViewById(R.id.btn_clear);

        btnClear.setOnClickListener(v -> {
            clearAll();
            updateDisplay(tvTotal, tvLast);
        });

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            deleteLastCharacter();
            updateDisplay(tvTotal, tvLast);
        });

        // Numpad nums
        initializeNumberButtons(tvTotal);
        initializeOperatorButtons(tvTotal);

        Button btnEquals = findViewById(R.id.btn_equals);
        btnEquals.setOnClickListener(v -> {
            performCalculation();
            updateDisplay(tvTotal, tvLast);
        });
    }

    private void clearAll() {
        total = 0;
        aux = "";
        last = "";
        calc = "";
    }

    private void deleteLastCharacter() {
        if (!aux.isEmpty()) {
            aux = aux.substring(0, aux.length() - 1);
        }
    }

    private void initializeNumberButtons(TextView tvTotal) {
        int[] numberButtonIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_dot};
        for (int numberButtonId : numberButtonIds) {
            Button button = findViewById(numberButtonId);
            CharSequence text = button.getText();
            button.setOnClickListener(v -> {
                if (tvTotal.getText().toString().equals("Syntax Error") || tvTotal.getText().toString().equals("NaN") || tvTotal.getText().toString().equals(String.valueOf(total))) {
                    aux = "";
                }
                onNumberButtonClick(text.toString());
                updateDisplay(tvTotal, null);
            });
        }
    }

    private void initializeOperatorButtons(TextView tvTotal) {
        int[] operatorButtonIds = {R.id.btn_subs, R.id.btn_divide, R.id.btn_plus, R.id.btn_multi};
        for (int operatorButtonId : operatorButtonIds) {
            Button button = findViewById(operatorButtonId);
            CharSequence text = button.getText();
            button.setOnClickListener(v -> {
                if (tvTotal.getText().toString().equals("Syntax Error") || tvTotal.getText().toString().equals("NaN") || tvTotal.getText().toString().equals(String.valueOf(total))) {
                    aux = "";
                }
                onOperatorButtonClick(text.toString());
                updateDisplay(tvTotal, null);
            });
        }
    }

    private void onNumberButtonClick(String number) {
        if (aux.equals("0") || aux.equals("Syntax Error") || aux.equals("NaN")) {
            aux = "";
        }
        if (last.isEmpty()) {
            total = 0;
        }
        calc += number;
        aux += number;
    }

    private void onOperatorButtonClick(String operator) {
        if (!aux.isEmpty()) {
            char lastChar = aux.charAt(aux.length() - 1);
            if (isOperator(String.valueOf(lastChar))) {
                // Reemplazar el último operador
                aux = aux.substring(0, aux.length() - 1);
                calc = calc.substring(0, calc.length() - 2); // Borra el espacio y el último operador en calc
            }
        }
        last = operator;
        aux += operator;
        if (operator.equalsIgnoreCase("X")) {
            operator = "*";
        } else if (operator.equals("÷")) {
            operator = "/";
        }
        calc += " " + operator + " ";
    }


    private void performCalculation() {
        total = calculate(calc);
        last = aux;
        if (Double.isNaN(total)) {
            aux = "Syntax Error";
        } else {
            aux = formatResult(total);
        }
        calc = aux; // Almacena el resultado actual en calc
    }

    @SuppressLint("DefaultLocale")
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%s", result);
        }
    }


    private double calculate(String expr) {
        Stack<Character> operators = new Stack<>();
        Stack<Double> values = new Stack<>();
        String[] tokens = expr.split(" ");

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            } else if (isNumber(token)) {
                double number = Double.parseDouble(token);
                values.push(number);
            } else if (isOperator(token)) {
                char operator = token.charAt(0);
                while (!operators.isEmpty() && hasPrecedence(operator, operators.peek())) {
                    applyOperator(operators.pop(), values);
                }
                operators.push(operator);
            } else {
                return Double.NaN; // Invalid token
            }
        }

        while (!operators.isEmpty()) {
            applyOperator(operators.pop(), values);
        }

        if (values.size() == 1) {
            return values.pop();
        } else {
            return Double.NaN; // Incomplete expression
        }
    }

    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOperator(String token) {
        return token.length() == 1 && "*/+-".contains(token);
    }

    private boolean hasPrecedence(char op1, char op2) {
        return (op1 == '+' || op1 == '-') && (op2 == '*' || op2 == '/');
    }

    private void applyOperator(char operator, Stack<Double> values) {
        if (values.size() < 2) {
            aux = "Syntax Error";
            return;
        }
        double operand2 = values.pop();
        double operand1 = values.pop();
        double result = performOperation(operand1, operand2, operator);
        values.push(result);
    }

    private double performOperation(double operand1, double operand2, char operator) {
        switch (operator) {
            case '*':
                return operand1 * operand2;
            case '/':
                if (operand2 != 0) {
                    return operand1 / operand2;
                } else {
                    aux = "Syntax Error";
                    return Double.NaN;
                }
            case '+':
                return operand1 + operand2;
            case '-':
                return operand1 - operand2;
            default:
                aux = "Syntax Error";
                return Double.NaN;
        }
    }

    private void updateDisplay(TextView tvTotal, TextView tvLast) {
        tvTotal.setText(aux.isEmpty() ? "0" : aux);
        if (tvLast != null) {
            tvLast.setText(last);
        }
    }
}
