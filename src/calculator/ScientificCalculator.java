package calculator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScientificCalculator extends JFrame {

    private JTextField displayField;
    private String currentInput;
    private boolean isNewInput;

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLayout(new BorderLayout());

        displayField = new JTextField();
        displayField.setEditable(false);
        add(displayField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4));

        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "sin", "cos", "tan", "sqrt"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new CalculatorButtonListener());
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
        currentInput = "";
        isNewInput = true;
    }

    private class CalculatorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String buttonText = ((JButton) e.getSource()).getText();

            switch (buttonText) {
                case "=":
                    evaluateExpression();
                    break;
                case "sqrt":
                    currentInput = String.valueOf(Math.sqrt(Double.parseDouble(currentInput)));
                    break;
                case "sin":
                    currentInput = String.valueOf(Math.sin(Math.toRadians(Double.parseDouble(currentInput))));
                    break;
                case "cos":
                    currentInput = String.valueOf(Math.cos(Math.toRadians(Double.parseDouble(currentInput))));
                    break;
                case "tan":
                    currentInput = String.valueOf(Math.tan(Math.toRadians(Double.parseDouble(currentInput))));
                    break;
                default:
                    handleNumericInput(buttonText);
            }

            displayField.setText(currentInput);
        }

        private void evaluateExpression() {
            try {
                currentInput = String.valueOf(eval(currentInput));
            } catch (Exception ex) {
                currentInput = "Error";
            }
            isNewInput = true;
        }

        private void handleNumericInput(String buttonText) {
            if (isNewInput) {
                currentInput = "";
                isNewInput = false;
            }
            currentInput += buttonText;
        }

        private double eval(String expression) {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
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
                    if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (; ; ) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (; ; ) {
                        if (eat('*')) x *= parseFactor();
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
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                        while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) nextChar();
                        String func = expression.substring(startPos, this.pos);
                        x = parseFactor();
                        switch (func) {
                            case "sqrt":
                                x = Math.sqrt(x);
                                break;
                            case "sin":
                                x = Math.sin(Math.toRadians(x));
                                break;
                            case "cos":
                                x = Math.cos(Math.toRadians(x));
                                break;
                            case "tan":
                                x = Math.tan(Math.toRadians(x));
                                break;
                            default:
                                throw new RuntimeException("Unknown function: " + func);
                        }
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    if (eat('^')) x = Math.pow(x, parseFactor());

                    return x;
                }
            }.parse();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ScientificCalculator calculator = new ScientificCalculator();
            calculator.setVisible(true);
        });
    }
}