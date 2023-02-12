package com.cpunisher.hasakafix.edit.editor;

import java.util.Set;

public interface IEditor<T, U> {

    public Set<U> getEdits(T oldTree, T newTree);
}
