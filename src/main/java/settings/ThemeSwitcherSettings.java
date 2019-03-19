package settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;

@State(
        name = "Theme Switcher Settings",
        storages = @Storage("/theme_switcher_config.xml")

)
public class ThemeSwitcherSettings implements PersistentStateComponent<ThemeSwitcherSettings> {

    int startDarkHour = 18;
    int startDarkMinutes = 18;

    int endDarkHour = 5;
    int endDarkMinutes = 5;

    public String lightColorScheme;
    public String darkColorScheme;

    public ThemeSwitcherSettings(){
        EditorColorsManager manager = EditorColorsManager.getInstance();

        lightColorScheme = manager.getScheme(EditorColorsManager.DEFAULT_SCHEME_NAME).getName();
        darkColorScheme = manager.getScheme("Darcula").getName();
    }


    public LocalTime getStartDarkTime() {
        return LocalTime.of(startDarkHour, startDarkMinutes);
    }

    public LocalTime getEndDarkTime() {
        return LocalTime.of(endDarkHour, endDarkMinutes);
    }


    @Nullable
    @Override
    public ThemeSwitcherSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ThemeSwitcherSettings themeSwitcherSettings) {
        XmlSerializerUtil.copyBean(themeSwitcherSettings, this);
    }
}
