package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.ResourceQuery;
import com.cyberark.conjur.util.EncodeUriComponent;

import java.net.URI;

/**
 * Maps {@link ResourceQuery} to resources endpoint URIs.
 */
final class ResourceQueryMapper {

    private ResourceQueryMapper() {
    }

    static URI toListUri(URI resourcesUri, ResourceQuery query) {
        return toUri(resourcesUri, query, false);
    }

    static URI toCountUri(URI resourcesUri, ResourceQuery query) {
        return toUri(resourcesUri, query, true);
    }

    private static URI toUri(URI resourcesUri, ResourceQuery query, boolean count) {
        StringBuilder uriBuilder = new StringBuilder(resourcesUri.toString());
        String separator = "?";

        if (count) {
            uriBuilder.append(separator).append("count=true");
            separator = "&";
        }

        if (query != null) {
            if (hasText(query.getKind())) {
                uriBuilder.append(separator).append("kind=").append(encodeQueryValue(query.getKind()));
                separator = "&";
            }
            if (hasText(query.getSearch())) {
                uriBuilder.append(separator).append("search=").append(encodeQueryValue(query.getSearch()));
                separator = "&";
            }
            if (hasText(query.getActingAs())) {
                uriBuilder.append(separator).append("acting_as=").append(encodeQueryValue(query.getActingAs()));
                separator = "&";
            }
            if (!count && query.getLimit() != null) {
                uriBuilder.append(separator).append("limit=").append(query.getLimit());
                separator = "&";
            }
            if (!count && query.getOffset() != null) {
                uriBuilder.append(separator).append("offset=").append(query.getOffset());
            }
        }

        return URI.create(uriBuilder.toString());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isEmpty();
    }

    private static String encodeQueryValue(String value) {
        return EncodeUriComponent.encodeUriComponent(value).replaceAll("\\+", "%20");
    }
}

