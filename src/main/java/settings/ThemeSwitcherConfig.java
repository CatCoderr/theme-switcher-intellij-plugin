package settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.Scheme;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

public class ThemeSwitcherConfig implements SearchableConfigurable, Configurable.NoScroll {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private JPanel panel;
    private JComboBox<String> lightColorSchemes;
    private JComboBox<String> darkColorSchemes;
    private JTextField startDarkField;
    private JTextField endDarkField;
    private JCheckBox followDarkMode;

    private ThemeSwitcherSettings settings = ServiceManager.getService(ThemeSwitcherSettings.class);
    private EditorColorsManager colorsManager = EditorColorsManager.getInstance();
    private boolean modified;

    @NotNull
    @Override
    public String getId() {
        return getDisplayName();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Theme Switcher Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        Vector<String> supportedSchemes =
                Arrays.stream(colorsManager.getAllSchemes())
                        .map(Scheme::getName)
                        .collect(Collectors.toCollection(Vector::new));

        lightColorSchemes.setModel(new DefaultComboBoxModel<>(supportedSchemes));
        darkColorSchemes.setModel(new DefaultComboBoxModel<>(supportedSchemes));

        reset();

        darkColorSchemes.addActionListener((event) -> modified = true);
        lightColorSchemes.addActionListener((event) -> modified = true);

        startDarkField.addActionListener((event) -> modified = true);
        endDarkField.addActionListener((event) -> modified = true);

        followDarkMode.setEnabled(SystemInfo.isMacOSMojave);

        followDarkMode.addActionListener((event) -> modified = true);

        followDarkMode.addItemListener(e -> {
            startDarkField.setEnabled(!followDarkMode.isSelected());
            endDarkField.setEnabled(!followDarkMode.isSelected());
        });

        return panel;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void reset() {
        lightColorSchemes.setSelectedItem(settings.lightColorScheme);
        darkColorSchemes.setSelectedItem(settings.darkColorScheme);

        startDarkField.setText(LocalTime.now()
                .with(ChronoField.HOUR_OF_DAY, settings.startDarkHour)
                .with(ChronoField.MINUTE_OF_HOUR, settings.startDarkMinutes)
                .format(DATE_TIME_FORMATTER));
        endDarkField.setText(LocalTime.now()
                .with(ChronoField.HOUR_OF_DAY, settings.endDarkHour)
                .with(ChronoField.MINUTE_OF_HOUR, settings.endDarkMinutes)
                .format(DATE_TIME_FORMATTER));

        followDarkMode.setSelected(settings.followMacOsDarkMode);

        startDarkField.setEnabled(!followDarkMode.isSelected());
        endDarkField.setEnabled(!followDarkMode.isSelected());


    }

    @Override
    public void apply() throws ConfigurationException {
        settings.lightColorScheme = (String) lightColorSchemes.getSelectedItem();
        settings.darkColorScheme = (String) darkColorSchemes.getSelectedItem();
        settings.followMacOsDarkMode = followDarkMode.isSelected();

        try {
            TemporalAccessor start = DATE_TIME_FORMATTER.parse(startDarkField.getText());
            TemporalAccessor end = DATE_TIME_FORMATTER.parse(endDarkField.getText());

            settings.startDarkHour = start.get(ChronoField.HOUR_OF_DAY);
            settings.startDarkMinutes = start.get(ChronoField.MINUTE_OF_HOUR);

            settings.endDarkHour = end.get(ChronoField.HOUR_OF_DAY);
            settings.endDarkMinutes = end.get(ChronoField.MINUTE_OF_HOUR);

        } catch (DateTimeParseException ex) {
            throw new ConfigurationException(ex.getMessage(), ex, "Invalid time specified!");
        }

    }
}
