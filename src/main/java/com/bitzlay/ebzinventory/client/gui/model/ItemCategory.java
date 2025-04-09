package com.bitzlay.ebzinventory.client.gui.model;

import net.minecraft.world.item.Item;

/**
 * Clase que representa una categoría de items para el sistema de crafteo.
 * Separada en una clase propia para mejorar la organización y permitir
 * mejor extensibilidad.
 */
public class ItemCategory {
    /** Identificador único de la categoría */
    private final String id;

    /** Nombre para mostrar de la categoría */
    private final String name;

    /** Icono que representa a la categoría */
    private final Item icon;

    /**
     * Constructor de la categoría de items.
     *
     * @param id Identificador único
     * @param name Nombre para mostrar
     * @param icon Item que representa a la categoría
     */
    public ItemCategory(String id, String name, Item icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    /**
     * Obtiene el identificador de la categoría.
     *
     * @return ID único de la categoría
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el nombre para mostrar de la categoría.
     *
     * @return Nombre de la categoría
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene el item que sirve como icono para la categoría.
     *
     * @return Item que representa la categoría
     */
    public Item getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "ItemCategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemCategory that = (ItemCategory) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}