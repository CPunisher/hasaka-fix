package com.cpunisher.hasakafix.edit.parser;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;

import java.io.IOException;

public class GTSourceParser implements ISourceParser<Tree> {
    static {
        TreeGenerators.getInstance().install(JdtTreeGenerator.class, JdtTreeGenerator.class.getAnnotation(Register.class));
        Run.initGenerators();
    }

    private final String extension;
    private final TreeGenerator generator;

    public GTSourceParser(String extension) {
        this.extension = extension;
        this.generator = TreeGenerators.getInstance().get(extension);
        if (this.generator == null) {
            throw new IllegalArgumentException("Can not find generator for extension " + extension);
        }
    }

    @Override
    public Tree parse(String source) {
        try {
            TreeContext context  = generator.generateFrom().string(source);
            return context.getRoot();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getLanguage() {
        return extension.substring(1);
    }
}
