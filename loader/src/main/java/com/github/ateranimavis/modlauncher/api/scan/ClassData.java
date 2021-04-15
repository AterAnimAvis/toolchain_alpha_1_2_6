/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.github.ateranimavis.modlauncher.api.scan;

import java.util.Objects;
import java.util.Set;

import org.objectweb.asm.Type;

public class ClassData {
    private final Type clazz;
    private final Type parent;
    private final Set<Type> interfaces;

    public ClassData(final Type clazz, final Type parent, final Set<Type> interfaces) {
        this.clazz = clazz;
        this.parent = parent;
        this.interfaces = interfaces;
    }

    public Type getClazz() {
        return clazz;
    }

    public Type getParent() {
        return parent;
    }

    public Set<Type> getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean equals(final Object obj) {
        try {
            return (!Objects.isNull(obj)) && Objects.equals(clazz, ((ClassData) obj).clazz);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.clazz);
    }
}