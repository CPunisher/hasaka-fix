package com.cpunisher.hasakafix.edit.parser;

public interface ISourceParser<T> {
    public T parse(String source);

    public String getLanguage();
}
