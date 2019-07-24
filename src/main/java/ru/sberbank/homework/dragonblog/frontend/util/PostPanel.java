package ru.sberbank.homework.dragonblog.frontend.util;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import ru.sberbank.homework.dragonblog.frontend.model.UiComment;
import ru.sberbank.homework.dragonblog.frontend.model.UiPost;
import ru.sberbank.homework.dragonblog.frontend.model.UiUser;
import ru.sberbank.homework.dragonblog.service.CommentServiceImpl;
import ru.sberbank.homework.dragonblog.service.PostServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PostPanel {
    private PostServiceImpl postService;
    private CommentServiceImpl commentService;

    private UiUser user;
    private UiPost post;
    private FormLayout contentLayout;
    private VerticalLayout postContent = new VerticalLayout();

    private Panel panel;
    private Label text;
    private TextArea textArea;
    private Button delete;
    private Button save;
    private Button edit;
    private Button createCommBut;
    private Panel  panelCreateComment;
    private Button openComment;

    public PostPanel(PostServiceImpl postService, CommentServiceImpl commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    public Panel getPanelPost(UiPost post, UiUser user, FormLayout contentLayout) {
        this.post = post;
        this.user = user;
        this.contentLayout = contentLayout;

        init();

        postContent.addComponent(text);
        postContent.addComponent(textArea);

        if(post.getAuthor().getId().equals(user.getId())) {
            HorizontalLayout buttons = formButton();
            postContent.addComponent(buttons);
        }

        postContent.addComponent(openComment);
        postContent.setComponentAlignment(openComment, Alignment.MIDDLE_CENTER);

        displayComments();

        postContent.addComponent(panelCreateComment);
        postContent.addComponent(createCommBut);

        panel.setContent(postContent);

        return panel;
    }

    private void init() {
        initPanel();
        initText();
        initOpenCommentButton();
        initCreateCommentPanel();
        initCreateCommentButton();

        if(post.getAuthor().getId().equals(user.getId())) {
            initTextArea();
            initButtonDelete();
            initButtonEdit();
            initButtonSave();
        }
    }

    private HorizontalLayout formButton() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSizeFull();
        buttons.setMargin(false);
        buttons.setStyleName("layout-with-bottom-border");

        buttons.addComponent(save);
        buttons.addComponent(edit);
        buttons.addComponent(delete);

        buttons.setComponentAlignment(edit, Alignment.MIDDLE_RIGHT);
        buttons.setExpandRatio(edit, 1.0f);
        buttons.setComponentAlignment(delete, Alignment.MIDDLE_RIGHT);
        return buttons;
    }

    private void initPanel() {
        UiUser author = post.getAuthor();

        panel = new Panel(author.getFirstName()
                + " "
                + author.getSurname()
                + " "
                + author.getNickname()
                + " "
                + post.getPostDateTime());

        panel.setSizeFull();
    }

    private void initText() {
        text = new Label(post.getDescription(), ContentMode.TEXT);
        text.setSizeFull();
    }

    private void initTextArea() {
        textArea = new TextArea();
        textArea.setValue(post.getDescription());
        textArea.setSizeFull();
        textArea.setVisible(false);
    }

    private void initButtonDelete() {
        delete = new Button();
        delete.setIcon(VaadinIcons.TRASH);
        delete.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        delete.setWidth("10px");
        delete.addClickListener((Button.ClickListener) event1 -> deletePost());
    }

    private void deletePost() {
        postService.delete(post.getId());
        contentLayout.removeComponent(panel);
    }

    private void initButtonEdit() {
        edit = new Button();
        edit.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        edit.setWidth("10px");
        edit.setIcon(VaadinIcons.EDIT);


        edit.addClickListener((Button.ClickListener) event1 -> {
            text.setVisible(false);
            textArea.setVisible(true);
            edit.setVisible(false);
            delete.setVisible(false);
            save.setVisible(true);
        });
    }

    private void initButtonSave() {
        save = new Button("Сохранить");
        save.setVisible(false);
        save.addClickListener((Button.ClickListener) event2 -> updatePost());
    }

    private void updatePost() {
        String newDescription = textArea.getValue();
        if(newDescription != null && !newDescription.isEmpty()) {
            post.setDescription(newDescription);
            postService.update(post, user.getId());
            text.setValue(newDescription);
            text.setVisible(true);
            textArea.setVisible(false);

            edit.setVisible(true);
            delete.setVisible(true);
            save.setVisible(false);
        }
    }

    private VerticalLayout formCommentPanel(UiComment comment) {
        CommentPanel commentPanel = new CommentPanel(commentService);
        return commentPanel.getPanelComment(comment, user, postContent);
    }

    private void initOpenCommentButton(){
        openComment = new Button("Все комментарии");
        openComment.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
    }

    private void displayComments() {
        UiComment comment = commentService.getFirstByDate(post.getId());

        if(comment != null) {
            postContent.addComponent(formCommentPanel(comment));
        }else {
            openComment.setVisible(false);
        }

        openComment.addClickListener((Button.ClickListener) event -> {
            if(comment != null) {
                List<UiComment> comments = commentService.getAllByPost(post.getId());

                int index = postContent.getComponentIndex(openComment) + 1;

                for (UiComment comment1 : comments) {
                    if (comment1.getId() < comment.getId()) {
                        VerticalLayout comLayout = formCommentPanel(comment1);
                        postContent.addComponent(comLayout, index);
                        index++;
                    }
                }
                openComment.setVisible(false);
            }
        });
    }

    private void initCreateCommentPanel() {
        panelCreateComment = new Panel("Создание нового комментария");
        panelCreateComment.setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();

        TextArea textArea = new TextArea();
        textArea.setPlaceholder("Начните писать ваш новый комментарий...");
        textArea.setSizeFull();

        Button create = new Button("Создать");

        create.addClickListener((Button.ClickListener) event2 -> {
            String newDescription = textArea.getValue();
            if(newDescription != null && !newDescription.isEmpty()) {
                textArea.setValue("");
                //Чтото тут с проверкой не так на автора.. вседа же тру будет;
                UiComment comment = UiComment.builder()
                        .author(user)
                        .post(post)
                        .description(newDescription)
                        .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy", Locale.getDefault())))
                        .build();

                comment = commentService.create(comment);
                postContent.addComponent(formCommentPanel(comment), postContent.getComponentIndex(panelCreateComment));
                panelCreateComment.setVisible(false);
                createCommBut.setVisible(true);
            }
        });

        verticalLayout.addComponent(textArea);
        verticalLayout.addComponent(create);
        panelCreateComment.setContent(verticalLayout);
        panelCreateComment.setVisible(false);
    }

    private void initCreateCommentButton(){
        createCommBut = new Button("Комментировать");
        createCommBut.addClickListener((Button.ClickListener) event -> {
            panelCreateComment.setVisible(true);
            createCommBut.setVisible(false);
        });
    }
}
