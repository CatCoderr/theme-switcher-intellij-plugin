/*
 * Copyright (c) 2019, CatCoder <https://github.com/CatCoderr>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 *   Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.catcoder.themeswitcher.plugin;

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
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.UIUtil;
import me.catcoder.themeswitcher.plugin.mac.MacOSDarkMode;
import org.jetbrains.annotations.NotNull;
import me.catcoder.themeswitcher.plugin.settings.ThemeSwitcherSettings;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
        if (SystemInfo.isMacOSMojave) {
            copyAndLoadNative("/native/MacOSDarkModeImpl.m.dylib");
        }

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
                boolean useDarkMode = (end.equals(start)) || end.getHour() < start.getHour() ? WITH_NEXT_DAY.test(start, end) : WITH_CURRENT_DAY.test(start, end);

                if (settings.followMacOsDarkMode && SystemInfo.isMacOSMojave) {
                    useDarkMode = MacOSDarkMode.isDarkModeEnabled();
                }

                UIManager.LookAndFeelInfo info = useDarkMode ? new DarculaLookAndFeelInfo() : new IntelliJLookAndFeelInfo();

                applyLaf(info);

                EditorColorsManager manager = EditorColorsManager.getInstance();

                manager.setGlobalScheme(manager.getScheme(useDarkMode ? settings.darkColorScheme : settings.lightColorScheme));

                SwingUtilities.invokeLater(() -> applyLaf(info));

            });
        };
    }

    private static void copyAndLoadNative(String path) {
        try {
            Path tempFile = Files
                    .createTempFile("native-", path.substring(path.lastIndexOf('.')));
            InputStream nativeLib = ThemeSwitcherApplication.class.getResourceAsStream(path);
            if (nativeLib == null) {
                throw new IllegalStateException("Native library " + path + " not found.");
            }

            Files.copy(nativeLib, tempFile, StandardCopyOption.REPLACE_EXISTING);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Well, it doesn't matter...
                }
            }));
            System.load(tempFile.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy natives", e);
        }
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
