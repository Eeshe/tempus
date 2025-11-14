package me.eeshe.tempus.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LayoutData;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextBox.TextChangeListener;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import me.eeshe.tempus.database.SQLiteManager;
import me.eeshe.tempus.model.TimerEntry;
import me.eeshe.tempus.service.TimerEntryService;

public class TimeTrackerScreen {
  private static final SimpleTheme TEXT_BOX_THEME = new SimpleTheme(
      TextColor.ANSI.BLACK,
      TextColor.ANSI.WHITE);

  private String projectName;
  private String clientName;
  private String description;
  private String task;
  private String email;
  private String tags;
  private boolean isBillable;

  private long initialTimeMillis;

  private Timer timerTask;

  private final TimerEntryService timerEntryService;

  public TimeTrackerScreen() {
    this.timerEntryService = new TimerEntryService(new SQLiteManager());
  }

  public void open(Screen screen) {
    open(screen, null);
  }

  public void open(Screen screen, TimerEntry timerEntry) {
    if (timerEntry != null) {
      this.projectName = timerEntry.getProjectName();
      this.clientName = timerEntry.getClientName();
      this.description = timerEntry.getDescription();
      this.task = timerEntry.getTask();
      this.email = timerEntry.getEmail();
      this.tags = String.join(", ", timerEntry.getTags());
      this.isBillable = timerEntry.isBillable();
    }

    WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
    textGUI.setTheme(new SimpleTheme(
        TextColor.ANSI.DEFAULT,
        TextColor.ANSI.DEFAULT));

    BasicWindow window = new BasicWindow();
    window.setHints(List.of(
        Window.Hint.CENTERED));

    Panel panel = new Panel(new LinearLayout());
    final LayoutData centeredLayoutData = LinearLayout.createLayoutData(Alignment.Center);

    Label titleLabel = new Label("Timer").setLayoutData(centeredLayoutData);
    panel.addComponent(titleLabel);

    Label projectLabel = new Label("Project");
    panel.addComponent(projectLabel);

    TextBox projectTextBox = createProjectTextBox();
    panel.addComponent(projectTextBox);

    Label clientLabel = new Label("Client");
    panel.addComponent(clientLabel);
    TextBox clientTextBox = createClientTextBox();
    panel.addComponent(clientTextBox);

    Label descriptionLabel = new Label("Description");
    panel.addComponent(descriptionLabel);
    TextBox descriptionTextBox = createDescriptionTextBox();
    panel.addComponent(descriptionTextBox);

    Label taskLabel = new Label("Task");
    panel.addComponent(taskLabel);
    TextBox taskTextBox = createTaskTextBox();
    panel.addComponent(taskTextBox);

    Label emailLabel = new Label("Email");
    panel.addComponent(emailLabel);
    TextBox emailTextBox = createEmailTextBox();
    panel.addComponent(emailTextBox);

    Label tagsLabel = new Label("Tags");
    panel.addComponent(tagsLabel);
    TextBox tagsTextBox = createTagsTextBox();
    panel.addComponent(tagsTextBox);

    panel.addComponent(createBillableCheckBox());

    Label elapsedTimeLabel = new Label(computeElapsedTimeText()).setLayoutData(centeredLayoutData);
    panel.addComponent(elapsedTimeLabel);

    panel.addComponent(
        new Separator(Direction.HORIZONTAL).setLayoutData(LinearLayout.createLayoutData(Alignment.Fill)));

    Label lastTimerLabel = new Label("");
    panel.addComponent(lastTimerLabel);

    Button timerButton = new Button("Start").setLayoutData(centeredLayoutData);
    timerButton.addListener(button -> {
      if (!isTimerTaskRunning()) {
        initialTimeMillis = System.currentTimeMillis();
        startTimerTask(elapsedTimeLabel);
        button.setLabel("Stop");
      } else {
        stopTimerTask();
        button.setLabel("Start");

        saveTimerEntry();

        lastTimerLabel
            .setText(String.format("Worked on %s:%s (%s) for: %s",
                descriptionTextBox.getText(),
                taskTextBox.getText(),
                projectTextBox.getText(),
                computeElapsedTimeText()));
        initialTimeMillis = 0;
        elapsedTimeLabel.setText(computeElapsedTimeText());
      }
    });
    panel.addComponent(timerButton);
    window.setComponent(panel);
    textGUI.addListener(new TextGUI.Listener() {
      @Override
      public boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
        if (keyStroke == null) {
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.Escape) {
          window.close();
        }
        return true;
      }
    });
    textGUI.addWindowAndWait(window);
  }

  private TextBox createProjectTextBox() {
    return createTextBox(
        projectName,
        (newText, changedByUserInteraction) -> {
          this.projectName = newText;
        });
  }

  private TextBox createClientTextBox() {
    return createTextBox(
        clientName,
        (newText, changedByUserInteraction) -> {
          this.clientName = newText;
        });
  }

  private TextBox createDescriptionTextBox() {
    return createTextBox(
        description,
        (newText, changedByUserInteraction) -> {
          this.description = newText;
        });
  }

  private TextBox createTaskTextBox() {
    return createTextBox(
        task,
        (newText, changedByUserInteraction) -> {
          this.task = newText;
        });
  }

  private TextBox createEmailTextBox() {
    return createTextBox(
        email,
        (newText, changedByUserInteraction) -> {
          this.email = newText;
        });
  }

  private TextBox createTagsTextBox() {
    return createTextBox(
        tags,
        (newText, changedByUserInteraction) -> {
          this.tags = newText;
        });
  }

  private TextBox createTextBox(String text, TextChangeListener textChangeListener) {
    TextBox textBox = new TextBox(new TerminalSize(50, 1));
    if (text != null) {
      textBox.setText(text);
    }
    textBox.setLayoutData(LinearLayout.createLayoutData(Alignment.Center));
    textBox.setTheme(TEXT_BOX_THEME);
    textBox.setTextChangeListener(textChangeListener);

    return textBox;
  }

  private CheckBox createBillableCheckBox() {
    CheckBox billableCheckBox = new CheckBox("Billable");
    billableCheckBox.setChecked(isBillable);
    billableCheckBox.addListener(checked -> {
      this.isBillable = checked;
    });

    return billableCheckBox;
  }

  private void saveTimerEntry() {
    if (projectName == null) {
      return;
    }
    TimerEntry timerEntry = new TimerEntry(
        projectName,
        clientName,
        description,
        task,
        email,
        tags == null ? List.of() : Arrays.asList(tags.split(",")),
        isBillable,
        initialTimeMillis,
        System.currentTimeMillis() - initialTimeMillis);
    timerEntryService.save(timerEntry);
  }

  private boolean isTimerTaskRunning() {
    return timerTask != null;
  }

  private void startTimerTask(Label elapsedTimeLabel) {
    if (timerTask != null) {
      timerTask.cancel();
    }
    timerTask = new Timer();
    timerTask.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        elapsedTimeLabel.setText(computeElapsedTimeText());
      }
    }, 0L, 1000);
  }

  private void stopTimerTask() {
    if (timerTask == null) {
      return;
    }
    timerTask.cancel();
    timerTask = null;
  }

  private String computeElapsedTimeText() {
    long elapsedTimeMillis = 0;
    if (initialTimeMillis != 0) {
      elapsedTimeMillis = System.currentTimeMillis() - initialTimeMillis;
    }
    return millisecondsToHMS(elapsedTimeMillis);
  }

  private String millisecondsToHMS(long milliseconds) {
    if (milliseconds < 0) {
      milliseconds = 0;
    }
    long totalSeconds = milliseconds / 1000;

    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;

    // Use String.format to ensure two digits with leading zeros
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }
}
