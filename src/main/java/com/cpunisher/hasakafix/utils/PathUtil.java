package com.cpunisher.hasakafix.utils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

public class PathUtil {
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
