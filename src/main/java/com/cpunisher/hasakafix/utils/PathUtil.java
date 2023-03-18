package com.cpunisher.hasakafix.utils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

public class PathUtil {
    public static Path removeExtension(Path path) {
        String strPath = path.toString();
        int index = strPath.lastIndexOf('.');
        if (index > 0) return Path.of(strPath.substring(0, index));
        return path;
    }

    public static Optional<String> getResourcePath(String resource) {
        return Optional.ofNullable(
                PathUtil.class.getClassLoader().getResource(resource)
        ).map(url -> {
            try {
                return Path.of(url.toURI()).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
