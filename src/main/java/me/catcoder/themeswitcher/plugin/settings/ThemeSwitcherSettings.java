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

package me.catcoder.themeswitcher.plugin.settings;

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

    //feature only available for MacOS Mojave
    public boolean followMacOsDarkMode;

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
