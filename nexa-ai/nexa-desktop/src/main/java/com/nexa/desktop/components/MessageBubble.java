package com.nexa.desktop.components;

import com.nexa.desktop.model.MessageDto;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;

public class MessageBubble extends HBox {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MessageDto message;
    private final boolean isUser;
    private TextFlow textFlow;
    private Label timestampLabel;
    private Button copyButton;

    public MessageBubble(MessageDto message) {
        this.message = message;
        this.isUser = "USER".equalsIgnoreCase(message.getRole());

        initLayout();
    }

    public static MessageBubble create(MessageDto message) {
        return new MessageBubble(message);
    }

    private void initLayout() {
        this.setPadding(new Insets(6, 16, 6, 16));
        this.setSpacing(12);
        this.setFillHeight(false);

        // Avatar
        StackPane avatar = createAvatar();

        // Content bubble box
        VBox bubbleBox = new VBox(6);
        bubbleBox.getStyleClass().add("message-bubble");
        bubbleBox.getStyleClass().add(isUser ? "message-bubble-user" : "message-bubble-assistant");
        bubbleBox.setMaxWidth(680);
        bubbleBox.setPadding(new Insets(12, 16, 10, 16));

        // Header: Role Badge + Model tag (for assistant)
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label(isUser ? "You" : "NEXA AI");
        roleLabel.getStyleClass().add(isUser ? "bubble-role-user" : "bubble-role-assistant");

        header.getChildren().add(roleLabel);

        if (!isUser && message.getAiModel() != null && !message.getAiModel().isBlank()) {
            Label modelBadge = new Label(message.getAiModel());
            modelBadge.getStyleClass().add("bubble-model-badge");
            header.getChildren().add(modelBadge);
        }

        // Text Flow
        textFlow = new TextFlow();
        textFlow.getStyleClass().add("bubble-text-flow");
        renderFormattedText(textFlow, message.getContent() != null ? message.getContent() : "");

        // Footer: Timestamp & Copy Button
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        String timeStr = message.getCreatedAt() != null ? message.getCreatedAt().format(TIME_FORMATTER) : "";
        timestampLabel = new Label(timeStr);
        timestampLabel.getStyleClass().add("bubble-timestamp");

        copyButton = new Button("Copy");
        copyButton.getStyleClass().add("bubble-copy-button");
        copyButton.setOnAction(e -> copyToClipboard());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isUser) {
            footer.getChildren().addAll(spacer, timestampLabel);
        } else {
            footer.getChildren().addAll(timestampLabel, spacer, copyButton);
        }

        bubbleBox.getChildren().addAll(header, textFlow, footer);

        if (isUser) {
            this.setAlignment(Pos.TOP_RIGHT);
            Region leftSpacer = new Region();
            HBox.setHgrow(leftSpacer, Priority.ALWAYS);
            this.getChildren().addAll(leftSpacer, bubbleBox, avatar);
        } else {
            this.setAlignment(Pos.TOP_LEFT);
            Region rightSpacer = new Region();
            HBox.setHgrow(rightSpacer, Priority.ALWAYS);
            this.getChildren().addAll(avatar, bubbleBox, rightSpacer);
        }
    }

    private StackPane createAvatar() {
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(36, 36);
        avatarPane.setMinSize(36, 36);
        avatarPane.setMaxSize(36, 36);

        Circle bg = new Circle(18);
        Label initialLabel = new Label();
        initialLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        if (isUser) {
            bg.setFill(Color.web("#374151"));
            initialLabel.setText("U");
            initialLabel.setTextFill(Color.WHITE);
        } else {
            bg.setFill(Color.web("#7167F8"));
            initialLabel.setText("NX");
            initialLabel.setTextFill(Color.WHITE);
        }

        avatarPane.getChildren().addAll(bg, initialLabel);
        return avatarPane;
    }

    public void updateContent(String text) {
        if (message != null) {
            message.setContent(text);
        }
        textFlow.getChildren().clear();
        renderFormattedText(textFlow, text != null ? text : "");
    }

    public void appendChunk(String chunk) {
        if (chunk == null) return;
        String newText = (message.getContent() != null ? message.getContent() : "") + chunk;
        updateContent(newText);
    }

    private void renderFormattedText(TextFlow flow, String rawText) {
        // Simple Markdown parser for bold (**), code blocks (```), and inline code (`)
        String[] lines = rawText.split("\n", -1);
        boolean inCodeBlock = false;
        StringBuilder codeBlockContent = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    addCodeBlock(flow, codeBlockContent.toString());
                    codeBlockContent.setLength(0);
                    inCodeBlock = false;
                } else {
                    // Start code block
                    inCodeBlock = true;
                    codeBlockContent.setLength(0);
                }
                continue;
            }

            if (inCodeBlock) {
                codeBlockContent.append(line).append("\n");
                continue;
            }

            // Normal line with inline formatting
            parseInlineMarkdown(flow, line);
            if (i < lines.length - 1) {
                flow.getChildren().add(new Text("\n"));
            }
        }

        if (inCodeBlock && !codeBlockContent.isEmpty()) {
            addCodeBlock(flow, codeBlockContent.toString());
        }
    }

    private void parseInlineMarkdown(TextFlow flow, String line) {
        if (line.isEmpty()) {
            flow.getChildren().add(new Text(""));
            return;
        }

        // Tokenize by ` (code) and ** (bold)
        int idx = 0;
        int len = line.length();

        while (idx < len) {
            int nextBold = line.indexOf("**", idx);
            int nextCode = line.indexOf("`", idx);

            int nextSpecial = -1;
            boolean isBold = false;
            boolean isCode = false;

            if (nextBold != -1 && (nextCode == -1 || nextBold < nextCode)) {
                nextSpecial = nextBold;
                isBold = true;
            } else if (nextCode != -1) {
                nextSpecial = nextCode;
                isCode = true;
            }

            if (nextSpecial == -1) {
                // Add remaining plain text
                Text plain = new Text(line.substring(idx));
                plain.getStyleClass().add("bubble-text");
                flow.getChildren().add(plain);
                break;
            }

            if (nextSpecial > idx) {
                Text plain = new Text(line.substring(idx, nextSpecial));
                plain.getStyleClass().add("bubble-text");
                flow.getChildren().add(plain);
            }

            if (isBold) {
                int closingBold = line.indexOf("**", nextSpecial + 2);
                if (closingBold != -1) {
                    Text boldText = new Text(line.substring(nextSpecial + 2, closingBold));
                    boldText.getStyleClass().add("bubble-text-bold");
                    flow.getChildren().add(boldText);
                    idx = closingBold + 2;
                } else {
                    Text plain = new Text(line.substring(nextSpecial));
                    plain.getStyleClass().add("bubble-text");
                    flow.getChildren().add(plain);
                    break;
                }
            } else if (isCode) {
                int closingCode = line.indexOf("`", nextSpecial + 1);
                if (closingCode != -1) {
                    Text codeText = new Text(line.substring(nextSpecial + 1, closingCode));
                    codeText.getStyleClass().add("bubble-text-inline-code");
                    flow.getChildren().add(codeText);
                    idx = closingCode + 1;
                } else {
                    Text plain = new Text(line.substring(nextSpecial));
                    plain.getStyleClass().add("bubble-text");
                    flow.getChildren().add(plain);
                    break;
                }
            }
        }
    }

    private void addCodeBlock(TextFlow flow, String code) {
        Text codeText = new Text("\n" + code.stripTrailing() + "\n");
        codeText.getStyleClass().add("bubble-text-code-block");
        flow.getChildren().add(codeText);
    }

    private void copyToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(message.getContent() != null ? message.getContent() : "");
        clipboard.setContent(content);

        copyButton.setText("Copied!");
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> copyButton.setText("Copy"));
        pause.play();
    }
}
