package com.cyberark.conjur.api;

/**
 * Value object for Conjur resources query parameters.
 */
public final class ResourceQuery {

    private final String kind;
    private final String search;
    private final String actingAs;
    private final Integer limit;
    private final Integer offset;

    private ResourceQuery(Builder builder) {
        this.kind = builder.kind;
        this.search = builder.search;
        this.actingAs = builder.actingAs;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a query with no filters.
     *
     * @return an empty query that matches all resources
     */
    public static ResourceQuery all() {
        return builder().build();
    }

    public String getKind() {
        return kind;
    }

    public String getSearch() {
        return search;
    }

    public String getActingAs() {
        return actingAs;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public static final class Builder {
        private String kind;
        private String search;
        private String actingAs;
        private Integer limit;
        private Integer offset;

        private Builder() {
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder search(String search) {
            this.search = search;
            return this;
        }

        public Builder actingAs(String actingAs) {
            this.actingAs = actingAs;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public ResourceQuery build() {
            return new ResourceQuery(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "kind='" + kind + '\'' +
                    ", search='" + search + '\'' +
                    ", actingAs='" + actingAs + '\'' +
                    ", limit=" + limit +
                    ", offset=" + offset +
                    '}';
        }
    }
}

