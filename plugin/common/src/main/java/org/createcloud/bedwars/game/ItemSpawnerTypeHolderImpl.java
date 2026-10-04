/*
 * Copyright (C) 2025 CreateCloud
 *
 * This file is part of Screaming BedWars.
 *
 * Screaming BedWars is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Screaming BedWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Screaming BedWars. If not, see <https://www.gnu.org/licenses/>.
 */

package org.createcloud.bedwars.game;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.createcloud.bedwars.api.game.ItemSpawnerTypeHolder;
import org.createcloud.bedwars.api.game.LocalGame;
import org.createcloud.bedwars.api.variants.Variant;
import org.createcloud.bedwars.variants.VariantImpl;
import org.createcloud.bedwars.variants.VariantManagerImpl;

@Data
@Accessors(fluent = true)
public class ItemSpawnerTypeHolderImpl implements ItemSpawnerTypeHolder {
    private final @NotNull String configKey;

    @Override
    public @Nullable ItemSpawnerTypeImpl toSpawnerType(@NotNull LocalGame game) {
        return toSpawnerType(game.getGameVariant());
    }

    @Override
    public @Nullable ItemSpawnerTypeImpl toSpawnerType(@Nullable Variant variant) {
        if (variant == null) {
            variant = VariantManagerImpl.getInstance().getDefaultVariant();
        }
        if (!(variant instanceof VariantImpl)) {
            throw new IllegalArgumentException("Invalid variant type: " + variant);
        }
        return ((VariantImpl) variant).getItemSpawnerType(configKey);
    }
}
