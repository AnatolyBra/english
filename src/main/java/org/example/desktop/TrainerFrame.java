package org.example.desktop;

import org.example.data.LessonLoader;
import org.example.model.Lesson;
import org.example.quiz.AttemptResult;
import org.example.quiz.Mistake;
import org.example.quiz.QuizSession;
import org.example.quiz.RoundResult;
import org.example.quiz.Verdict;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public final class TrainerFrame extends JFrame {

    private static final String PAGE_LESSONS = "lessons";
    private static final String PAGE_QUIZ = "quiz";
    private static final String PAGE_SUMMARY = "summary";

    private static final Color OK = new Color(0x1B7F3A);
    private static final Color CLOSE = new Color(0xB45309);
    private static final Color WRONG = new Color(0xB91C1C);
    private static final Color MUTED = new Color(0x475569);

    private final List<Lesson> lessons;
    private Lesson currentLesson;
    private QuizSession session;

    private final CardLayout pages = new CardLayout();
    private final JPanel root = new JPanel(pages);

    private final DefaultListModel<Lesson> lessonModel = new DefaultListModel<>();
    private final JList<Lesson> lessonList = new JList<>(lessonModel);

    private final JLabel quizTitle = new JLabel();
    private final JLabel quizProgress = new JLabel();
    private final JTextArea prompt = plainArea(22);
    private final JTextField answer = new JTextField();
    private final JTextArea feedback = plainArea(15);
    private final JButton checkButton = new JButton("Check");
    private final JButton hintButton = new JButton("Hint");
    private final JButton skipButton = new JButton("Skip");
    private final JButton nextButton = new JButton("Next");

    private final JLabel scoreLabel = new JLabel();
    private final JTextArea mistakesArea = plainArea(15);
    private final JButton reviewButton = new JButton("Review mistakes");
    private final JButton againButton = new JButton("Again");
    private final JButton backButton = new JButton("Back to lessons");
    private final SuccessOverlay successOverlay = new SuccessOverlay();
    private final Timer fieldPulse = new Timer(24, event -> pulseField());
    private Color pulseFrom = Color.WHITE;
    private Color pulseTo;
    private float pulseProgress;
    private Color answerIdleBackground;

    public TrainerFrame() {
        super("English Trainer");
        this.lessons = LessonLoader.loadDefault();
        lessons.forEach(lessonModel::addElement);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 440));
        setSize(760, 540);
        setLocationRelativeTo(null);

        root.add(buildLessonPage(), PAGE_LESSONS);
        root.add(buildQuizPage(), PAGE_QUIZ);
        root.add(buildSummaryPage(), PAGE_SUMMARY);
        setContentPane(root);
        setGlassPane(successOverlay);
        successOverlay.setVisible(false);
        fieldPulse.setRepeats(true);
        showLessons();
    }

    private JPanel buildLessonPage() {
        JLabel heading = new JLabel("Choose a pack");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));

        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonList.setFont(lessonList.getFont().deriveFont(16f));
        lessonList.setCellRenderer(new LessonRenderer());
        lessonList.setSelectedIndex(0);
        lessonList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    startSelectedLesson();
                }
            }
        });

        JButton start = new JButton("Start");
        start.addActionListener(event -> startSelectedLesson());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(start, BorderLayout.EAST);

        JPanel page = padded();
        page.add(heading, BorderLayout.NORTH);
        page.add(new JScrollPane(lessonList), BorderLayout.CENTER);
        page.add(spacer(bottom), BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildQuizPage() {
        quizTitle.setFont(quizTitle.getFont().deriveFont(Font.BOLD, 16f));
        quizProgress.setForeground(MUTED);
        quizProgress.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.add(quizTitle, BorderLayout.CENTER);
        header.add(quizProgress, BorderLayout.EAST);

        prompt.setFont(prompt.getFont().deriveFont(Font.PLAIN, 24f));

        answer.setFont(answer.getFont().deriveFont(18f));
        answer.addActionListener(event -> onEnter());
        answer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showLessons();
                }
            }
        });

        feedback.setForeground(MUTED);

        checkButton.addActionListener(event -> checkAnswer());
        hintButton.addActionListener(event -> showHint());
        skipButton.addActionListener(event -> skipCard());
        nextButton.addActionListener(event -> goNext());
        nextButton.setVisible(false);

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(checkButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(hintButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(skipButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(nextButton);
        actions.add(Box.createHorizontalGlue());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        answer.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(Box.createVerticalGlue());
        center.add(prompt);
        center.add(Box.createVerticalStrut(20));
        center.add(answer);
        center.add(Box.createVerticalStrut(12));
        center.add(feedback);
        center.add(Box.createVerticalStrut(16));
        center.add(actions);
        center.add(Box.createVerticalGlue());

        JPanel page = padded();
        page.add(header, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildSummaryPage() {
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 22f));
        mistakesArea.setFont(mistakesArea.getFont().deriveFont(15f));

        reviewButton.addActionListener(event -> startReview());
        againButton.addActionListener(event -> startRound(false));
        backButton.addActionListener(event -> showLessons());

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(reviewButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(againButton);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(backButton);
        actions.add(Box.createHorizontalGlue());

        JPanel page = padded();
        page.add(scoreLabel, BorderLayout.NORTH);
        page.add(new JScrollPane(mistakesArea), BorderLayout.CENTER);
        page.add(spacer(actions), BorderLayout.SOUTH);
        return page;
    }

    private void startSelectedLesson() {
        Lesson selected = lessonList.getSelectedValue();
        if (selected == null) {
            return;
        }
        currentLesson = selected;
        startRound(false);
    }

    private void startRound(boolean review) {
        if (currentLesson == null) {
            return;
        }
        session = review
                ? QuizSession.review(currentLesson, session.result().missed())
                : QuizSession.firstPass(currentLesson);
        showCurrentCard();
        pages.show(root, PAGE_QUIZ);
        answer.requestFocusInWindow();
    }

    private void startReview() {
        if (session == null || session.result().missed().isEmpty()) {
            return;
        }
        startRound(true);
    }

    private void showCurrentCard() {
        if (session.isDone()) {
            showSummary();
            return;
        }
        quizTitle.setText(session.lesson().title() + (session.reviewMode() ? "  ·  review" : ""));
        quizProgress.setText(session.position() + " / " + session.size());
        prompt.setText(session.current().russian());
        answer.setText("");
        answer.setEnabled(true);
        if (answerIdleBackground == null) {
            answerIdleBackground = answer.getBackground();
        }
        resetAnswerField();
        feedback.setText(" ");
        feedback.setForeground(MUTED);
        successOverlay.stop();
        setAskingButtons(true);
        answer.requestFocusInWindow();
    }

    private void onEnter() {
        if (session != null && session.isFeedback()) {
            goNext();
        } else {
            checkAnswer();
        }
    }

    private void checkAnswer() {
        if (session == null || !session.isAsking()) {
            return;
        }
        applyAttempt(session.attempt(answer.getText()));
    }

    private void showHint() {
        if (session == null || !session.isAsking()) {
            return;
        }
        applyAttempt(session.attempt("?"));
    }

    private void skipCard() {
        if (session == null || !session.isAsking()) {
            return;
        }
        applyAttempt(session.attempt("s"));
    }

    private void applyAttempt(AttemptResult result) {
        switch (result.kind()) {
            case QUIT -> showLessons();
            case EMPTY -> {
                feedback.setForeground(MUTED);
                feedback.setText("Type the English phrase.");
            }
            case HINT -> {
                feedback.setForeground(MUTED);
                feedback.setText("Hint: " + result.hint());
                answer.requestFocusInWindow();
            }
            case SKIP, CHECKED -> showFeedback(result);
        }
    }

    private void showFeedback(AttemptResult result) {
        answer.setEnabled(false);
        setAskingButtons(false);
        if (result.check() != null && result.check().accepted()) {
            boolean exact = result.check().verdict() == Verdict.CORRECT;
            if (exact) {
                feedback.setForeground(OK);
                feedback.setText("Correct");
            } else {
                feedback.setForeground(CLOSE);
                feedback.setText("Close. Canonical: " + result.card().english());
            }
            playSuccess(exact);
        } else {
            feedback.setForeground(WRONG);
            StringBuilder text = new StringBuilder("Wrong. Answer: ").append(result.card().english());
            if (result.otherMatch() != null) {
                text.append("   ·   that phrase means: ").append(result.otherMatch().russian());
            }
            feedback.setText(text.toString());
        }
        nextButton.requestFocusInWindow();
    }

    private void goNext() {
        if (session == null || !session.isFeedback()) {
            return;
        }
        session.advance();
        showCurrentCard();
    }

    private void showSummary() {
        RoundResult result = session.result();
        scoreLabel.setText("Result: " + result.accepted() + " / " + result.total());
        if (result.mistakes().isEmpty()) {
            mistakesArea.setText("All correct.");
        } else {
            StringBuilder text = new StringBuilder();
            for (Mistake mistake : result.mistakes()) {
                text.append("· ").append(mistake.card().russian()).append('\n');
                if (!mistake.typed().isBlank()) {
                    text.append("  you: ").append(mistake.typed()).append('\n');
                }
                text.append("  answer: ").append(mistake.card().english()).append("\n\n");
            }
            mistakesArea.setText(text.toString());
            mistakesArea.setCaretPosition(0);
        }
        reviewButton.setEnabled(!result.missed().isEmpty());
        successOverlay.stop();
        pages.show(root, PAGE_SUMMARY);
    }

    private void showLessons() {
        session = null;
        successOverlay.stop();
        resetAnswerField();
        pages.show(root, PAGE_LESSONS);
        lessonList.requestFocusInWindow();
    }

    private void playSuccess(boolean exact) {
        Point onScreen = answer.getLocationOnScreen();
        Point origin = new Point(onScreen.x + answer.getWidth() / 2, onScreen.y + answer.getHeight() / 2);
        SwingUtilities.convertPointFromScreen(origin, successOverlay);
        successOverlay.play(origin.x, origin.y, exact);
        startFieldPulse(exact ? new Color(0xDCFCE7) : new Color(0xFEF3C7));
    }

    private void startFieldPulse(Color flash) {
        if (answerIdleBackground == null) {
            answerIdleBackground = answer.getBackground();
        }
        pulseFrom = flash;
        pulseTo = answerIdleBackground;
        pulseProgress = 0f;
        answer.setBackground(flash);
        fieldPulse.restart();
    }

    private void pulseField() {
        pulseProgress = Math.min(1f, pulseProgress + 0.06f);
        answer.setBackground(mix(pulseFrom, pulseTo, pulseProgress));
        if (pulseProgress >= 1f) {
            fieldPulse.stop();
            resetAnswerField();
        }
    }

    private void resetAnswerField() {
        fieldPulse.stop();
        if (answerIdleBackground != null) {
            answer.setBackground(answerIdleBackground);
        }
    }

    private static Color mix(Color from, Color to, float t) {
        return new Color(
                Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
                Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t)
        );
    }

    private void setAskingButtons(boolean asking) {
        checkButton.setVisible(asking);
        hintButton.setVisible(asking);
        skipButton.setVisible(asking);
        nextButton.setVisible(!asking);
    }

    private static JPanel padded() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        return panel;
    }

    private static JPanel spacer(JPanel inner) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private static JTextArea plainArea(int fontSize) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setFont(area.getFont().deriveFont((float) fontSize));
        return area;
    }

    private static final class LessonRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Lesson lesson) {
                setText(lesson.title() + "   (" + lesson.cards().size() + ")");
            }
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            return this;
        }
    }
}
