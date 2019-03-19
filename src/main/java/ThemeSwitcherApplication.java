import com.intellij.ide.WelcomeWizardUtil;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.laf.IntelliJLookAndFeelInfo;
import com.intellij.ide.ui.laf.LafManagerImpl;
import com.intellij.ide.ui.laf.darcula.DarculaLookAndFeelInfo;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import settings.ThemeSwitcherSettings;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

public class ThemeSwitcherApplication implements StartupActivity, DumbAware {

    private static final long UPDATE_INTERVAL_MS = 5 * 1000;

    private static final BiPredicate<LocalTime, LocalTime> WITH_NEXT_DAY = (start, end) -> {
        LocalDateTime current = LocalDateTime.now();

        LocalDateTime startDate = current
                .with(ChronoField.HOUR_OF_DAY, start.getHour())
                .with(ChronoField.MINUTE_OF_HOUR, start.getMinute()
                );

        LocalDateTime endDate = current
                .with(ChronoField.HOUR_OF_DAY, end.getHour())
                .with(ChronoField.MINUTE_OF_HOUR, end.getMinute())
                .plusDays(current.getHour() < end.getHour() ? 0 : 1);

        return current.getDayOfMonth() == endDate.getDayOfMonth() ?
                current.isBefore(endDate) : current.isAfter(startDate) && current.isBefore(endDate);

    };


    private static final BiPredicate<LocalTime, LocalTime> WITH_CURRENT_DAY = (start, end) -> {
        LocalTime time = LocalTime.now();

        return time.isAfter(start) && time.isBefore(end);
    };


    @Override
    public void runActivity(@NotNull Project project) {
        ScheduledExecutorService executor = AppExecutorUtil.getAppScheduledExecutorService();

        executor.scheduleWithFixedDelay(createUpdateTask(), UPDATE_INTERVAL_MS, UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private Runnable createUpdateTask() {
        return () -> {
            Application application = ApplicationManager.getApplication();
            ThemeSwitcherSettings settings = ServiceManager.getService(ThemeSwitcherSettings.class);

            LocalTime start = settings.getStartDarkTime();
            LocalTime end = settings.getEndDarkTime();

            application.invokeLater(() -> {
                boolean useDarkMode = (end.equals(start))
                        || end.getHour() < start.getHour() ?
                        WITH_NEXT_DAY.test(start, end) : WITH_CURRENT_DAY.test(start, end);

                UIManager.LookAndFeelInfo info = useDarkMode ? new DarculaLookAndFeelInfo() : new IntelliJLookAndFeelInfo();

                applyLaf(info);

                EditorColorsManager manager = EditorColorsManager.getInstance();

                SwingUtilities.invokeLater(() -> {
                    applyLaf(info);

                    manager.setGlobalScheme(manager.getScheme(useDarkMode ? settings.darkColorScheme : settings.lightColorScheme));
                });

            });
        };
    }

    private static void applyLaf(UIManager.LookAndFeelInfo info) {
        try {
            boolean wasUnderDarcula = UIUtil.isUnderDarcula();
            UIManager.setLookAndFeel(info.getClassName());

            LafManagerImpl.updateForDarcula(UIUtil.isUnderDarcula());
            WelcomeWizardUtil.setWizardLAF(info.getClassName());

            LafManager lafManager = LafManager.getInstance();
            lafManager.setCurrentLookAndFeel(info);

            if (lafManager instanceof LafManagerImpl) {
                ((LafManagerImpl) lafManager).updateWizardLAF(wasUnderDarcula);
            } else {
                lafManager.updateUI();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}