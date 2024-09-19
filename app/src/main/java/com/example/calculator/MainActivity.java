package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private TextView tvExpression;
    private String expression = ""; // Chuỗi để lưu biểu thức hiện tại
    private String lastInput = ""; // Lưu lại ký tự cuối cùng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tvExpression);

        // Đặt sự kiện cho các nút
        setButtonListeners();
    }

    private void setButtonListeners() {
        // Các nút số
        int[] numberButtonIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                expression += button.getText().toString();
                lastInput = button.getText().toString();
                tvExpression.setText(expression);
            }
        };
        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        // Thêm sự kiện cho nút "."
        findViewById(R.id.btnDecimal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chỉ cho phép thêm dấu "." nếu biểu thức không trống và không đã có dấu "."
                if (!expression.isEmpty() && !lastInput.equals(".")) {
                    expression += ".";
                    lastInput = ".";
                    tvExpression.setText(expression);
                }
            }
        });

        // Các nút phép toán
        int[] operatorButtonIds = {R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide};
        View.OnClickListener operatorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (!expression.isEmpty() && !isOperator(lastInput)) {
                    expression += button.getText().toString();
                    lastInput = button.getText().toString();
                    tvExpression.setText(expression);
                }
            }
        };
        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(operatorClickListener);
        }

        // Nút mở ngoặc
        findViewById(R.id.btnOpenParen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expression += "(";
                lastInput = "(";
                tvExpression.setText(expression);
            }
        });

        // Nút đóng ngoặc
        findViewById(R.id.btnCloseParen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expression += ")";
                lastInput = ")";
                tvExpression.setText(expression);
            }
        });

        // Nút xóa C
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expression = "";
                tvExpression.setText("");
                lastInput = "";
            }
        });

        // Nút xóa từng ký tự DEL
        findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expression.length() > 0) {
                    expression = expression.substring(0, expression.length() - 1);
                    lastInput = expression.isEmpty() ? "" : expression.substring(expression.length() - 1);
                    tvExpression.setText(expression);
                }
            }
        });

        // Nút bằng =
        findViewById(R.id.btnEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!expression.isEmpty() && !isOperator(lastInput)) {
                    calculateResult();
                }
            }
        });
    }

    // Hàm tính toán kết quả
    private void calculateResult() {
        try {
            double result = eval(expression); // Gọi hàm tính toán
            // Kiểm tra xem kết quả có phải là số nguyên không
            if (result == (int) result) {
                expression = String.valueOf((int) result); // Chuyển đổi thành số nguyên
            } else {
                expression = String.valueOf(result); // Giữ nguyên định dạng số thập phân
            }
            tvExpression.setText(expression); // Cập nhật hiển thị biểu thức
        } catch (Exception e) {
            tvExpression.setText("Error");
            expression = ""; // Đặt biểu thức về rỗng khi có lỗi
        }
    }

    // Hàm kiểm tra ký tự có phải là phép toán không
    private boolean isOperator(String input) {
        return input.equals("+") || input.equals("-") || input.equals("*") || input.equals("/");
    }

    // Hàm tính toán biểu thức đơn giản
    public double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }
        }.parse();
    }
}